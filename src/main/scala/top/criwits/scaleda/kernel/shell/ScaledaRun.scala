package top.criwits.scaleda
package kernel.shell

import idea.runner.ScaledaRuntimeInfo
import kernel.net.RemoteClient
import kernel.net.remote.Empty
import kernel.project.config.{ProjectConfig, TargetConfig, TaskConfig, TaskType}
import kernel.shell.command.{CommandDeps, CommandRunner, RemoteCommandDeps}
import kernel.toolchain.executor.{Executor, ImplementExecutor, SimulationExecutor, SynthesisExecutor}
import kernel.toolchain.{Toolchain, ToolchainProfile}
import kernel.utils.KernelLogger

import io.grpc.StatusRuntimeException

import java.io.File
import java.util.Date
import scala.collection.mutable.ArrayBuffer

object ScaledaRun {

  /** Run a task.
    * @param handler A [[ScaledaRunHandler]] used to redirect output and error
    * @param rt: Whether to run remotely: `rt.profile.host`
    */
  def runTask(
      handler: ScaledaRunHandler,
      rt: ScaledaRuntimeInfo
  ): Unit = {
    require(rt.profile.profileName.nonEmpty, "must provide profile before runTask")
    val remoteDeps =
      if (rt.profile.isRemoteProfile && ProjectConfig.projectBase.nonEmpty)
        Some(RemoteCommandDeps(new File(ProjectConfig.projectBase.get), host = rt.profile.host))
      else None
    KernelLogger.info(s"runTask workingDir=${rt.workingDir.getAbsoluteFile}")

    val info = Toolchain.toolchains(rt.target.toolchain)
    val rtProcessed =
      if (rt.task.preset) {
        // fetch remote system info
        val remoteInfo =
          if (rt.profile.isRemoteProfile) {
            val (client, shutdown) = RemoteClient(rt.profile.host)
            val remoteInfoReply    = client.getRemoteInfo(Empty.of())
            shutdown()
            Some(remoteInfoReply)
          } else None
        Toolchain.toolchainPresetHandler.get(rt.target.toolchain).flatMap(_.handlePreset(rt, remoteInfo))
      } else Some(rt)
    val toolchain = info._2(rt.executor)
    if (rtProcessed.isEmpty) {
      KernelLogger.warn(
        s"Cannot apply preset for ${rt.target.toolchain}! Preset supports: ${Toolchain.toolchainPresetHandler.keys}"
      )
    }
    rtProcessed.foreach(p => {
      val commands = toolchain.commands(p.task)
      CommandRunner.executeLocalOrRemote(remoteDeps, commands, handler)
    })
  }

  def runTaskBackground(
      handler: ScaledaRunHandler,
      runtime: ScaledaRuntimeInfo,
      daemon: Boolean = true
  ): Thread = {
    val t = new Thread(() => runTask(handler, runtime), s"run-task-${runtime.id}")
    t.setDaemon(daemon)
    t
  }

  def generateExecutor(
      target: TargetConfig,
      task: TaskConfig,
      profile: ToolchainProfile,
      workingDir: File
  ): Executor = {
    val workingDirName = target.name + "-" + task.name
    task.taskType match {
      case TaskType.Simulation =>
        // FIXME: GENERATE TESTBENCH?
        val testbench    = task.findTopModule.get // FIXME: should not get if None, but...
        val workingPlace = new File(new File(workingDir, ".sim"), workingDirName)
        SimulationExecutor(
          workingDir = workingPlace,
          topModule = testbench,
          vcdFile = new File(workingPlace, testbench + "_waveform.vcd"),
          profile = profile
        )
      case TaskType.Synthesis =>
        SynthesisExecutor(
          workingDir = new File(new File(workingDir, ".synth"), workingDirName),
          topModule = task.findTopModule.get,
          profile = profile
        )
      case TaskType.Implement =>
        ImplementExecutor(
          workingDir = new File(new File(workingDir, ".impl"), workingDirName),
          topModule = task.findTopModule.get,
          profile = profile
        )
    }
  }

  /** Generate a runtime from names.<br/>
    * May reach remote profiles.
    * @param targetName name of target in config
    * @param taskName name of task in config
    * @param profileName name of profile, local / remote
    * @param profileHost empty if local
    * @return
    */
  def generateRuntimeFromName(
      targetName: String,
      taskName: String,
      profileName: String,
      profileHost: String
  ): Option[ScaledaRuntimeInfo] = {
    val configOptional = ProjectConfig.getConfig()
    if (configOptional.isEmpty) {
      KernelLogger.warn("no configure found")
      return None
    }
    val c = configOptional.get
    c.taskByTaskTargetName(taskName, targetName)
      .flatMap(f => {
        val (target, task)                                = f
        var remoteProfiles: Option[Seq[ToolchainProfile]] = None
        val profileHostUse                                = task.host.getOrElse(profileHost)
        KernelLogger.info(s"profileHostUse: $profileHostUse")
        val profile =
          if (profileHostUse == null || profileHostUse.isEmpty) {
            // Run locally if no host argument provided
            Toolchain
              .profiles()
              .find(p => p.toolchainType == target.toolchain && (p.profileName == profileName || profileName.isEmpty))
          } else {
            // TODO: remote profiles request util
            try {
              val (client, shutdown) = RemoteClient(profileHostUse)
              try {
                remoteProfiles = Some(
                  client
                    .getProfiles(Empty())
                    .profiles
                    .map(p => ToolchainProfile.asRemoteToolchainProfile(p, profileHostUse))
                )
              } finally {
                shutdown()
              }
            } catch {
              case e: StatusRuntimeException =>
                KernelLogger.warn("Cannot load profiles from host", profileHostUse, e)
                return None
            }
            remoteProfiles.get
              .find(p => p.toolchainType == target.toolchain && (p.profileName == profileName || profileName.isEmpty))
          }
        if (profile.isEmpty) {
          KernelLogger.warn("Cannot specify profile! profiles:", Toolchain.profiles())
          None
        } else {
          val runtimeId =
            s"${target.toolchain}-${target.name}-${task.name}-${new Date()}"

          val workingDir = new File(ProjectConfig.projectBase.get)
          val executor   = ScaledaRun.generateExecutor(target, task, profile.get, workingDir)
          val runtime = ScaledaRuntimeInfo(
            id = runtimeId,
            target = target,
            task = task,
            profile = profile.get,
            executor = executor,
            workingDir = workingDir
          )

          Some(runtime)
        }
      })
  }
}

trait ScaledaRunHandler {
  def onStdout(data: String): Unit

  def onStderr(data: String): Unit

  /** Invoked when one return value captured, and `finishedAll` == true after all commands done
    * @param returnValue return value
    * @param finishedAll is all commands finished
    * @param meetErrors has errors when executing
    */
  def onReturn(returnValue: Int, finishedAll: Boolean, meetErrors: Boolean): Unit

  /** Return true if handler is stopping this process
    * @return terminating
    */
  def isTerminating: Boolean = false

  /** Invoked before every command call
    * @param command command
    */
  def onShellCommand(command: CommandDeps): Unit = {}

  def onStepDescription(data: String): Unit = {}

  def expectedReturnValue: Int = 0
}

/** RunHandler that logging outputs to [[KernelLogger]]
  */
trait ScaledaRunKernelHandlerWithReturn extends ScaledaRunHandler {
  override def onStdout(data: String): Unit = KernelLogger.info(data)

  override def onStderr(data: String): Unit = KernelLogger.error(data)
}

object ScaledaRunKernelHandler extends ScaledaRunKernelHandlerWithReturn {
  override def onReturn(returnValue: Int, finishedAll: Boolean, meetErrors: Boolean): Unit = {
    if (meetErrors)
      KernelLogger.warn(s"Command failed, returns $returnValue; finishedAll: $finishedAll, meetErrors: $meetErrors")
    else KernelLogger.info(s"Command done, returns $returnValue; finishedAll: $finishedAll, meetErrors: $meetErrors")
  }
}

object ScaledaRunKernelRemoteHandler extends ScaledaRunKernelHandlerWithReturn {
  override def onStdout(data: String): Unit = KernelLogger.info("[remote]", data)

  override def onStderr(data: String): Unit = KernelLogger.error("[remote]", data)

  override def onReturn(returnValue: Int, finishedAll: Boolean, meetErrors: Boolean): Unit = {
    if (meetErrors)
      KernelLogger.warn(
        s"Remote command failed, returns $returnValue; finishedAll: $finishedAll, meetErrors: $meetErrors"
      )
    else
      KernelLogger.info(
        s"Remote command done, returns $returnValue; finishedAll: $finishedAll, meetErrors: $meetErrors"
      )
  }
}

/** RunHandler that records return value and outputs to [[returnValues]]
  * @param returnValues optional array to store results
  * @param outputs array to store stdout outputs
  * @param errors array to store stderr outputs
  */
class ScaledaRunHandlerToArray(
    returnValues: Option[ArrayBuffer[Int]],
    outputs: ArrayBuffer[String],
    errors: Option[ArrayBuffer[String]] = None
) extends ScaledaRunHandler {
  override def onStdout(data: String): Unit = outputs.addOne(data)

  override def onStderr(data: String): Unit =
    errors.map(_.addOne(data)).getOrElse(onStdout(data))

  override def onReturn(returnValue: Int, finishedAll: Boolean, meetErrors: Boolean): Unit =
    returnValues.foreach(_.addOne(returnValue))
}
