package top.criwits.scaleda
package kernel.shell

import kernel.project.config.{ProjectConfig, TargetConfig, TaskConfig, TaskType}
import kernel.shell.command.{CommandDeps, CommandRunner}
import kernel.toolchain.Toolchain
import kernel.toolchain.executor.{SimulationExecutor, SynthesisExecutor}
import kernel.toolchain.impl.Vivado
import kernel.utils.KernelLogger

import java.io.File

object ScaledaRun {
  def runTask(
      handler: ScaledaRunHandler,
      workingDir: File,
      target: TargetConfig,
      task: TaskConfig
  ) = {
    KernelLogger.info(s"runTask workingDir=${workingDir.getAbsoluteFile}")
    val config = ProjectConfig.getConfig().get
    val info = Toolchain.toolchains(target.toolchain)
    // find profile
    Toolchain
      .profiles()
      .filter(p => p.toolchainType == target.toolchain) // TODO: if none? if multiple?
      .foreach(profile => {
        // generate executor
        val executor = task.getTaskType match {
          case TaskType.Simulation =>
            SimulationExecutor(
              workingDir = new File(workingDir, ".sim"),
              topModule = task.findTopModule.get, // TODO / FIXME: Exception if not valid
              profile = profile
            )
          case TaskType.Synthesis =>
            SynthesisExecutor(
              workingDir = new File(workingDir, ".synth"),
              topModule = task.findTopModule.get,
              profile = profile
            )
          case _ => {
            KernelLogger.error(s"unsupported task type: ${task.getTaskType}")
            ???
          }
        }
        if (task.preset) {
          target.toolchain match {
            case Vivado.internalID => {
              val r = new Vivado.TemplateRenderer(
                executor = executor,
                targetConfig = target,
                taskConfig = task
              )
              r.render()
            }
            case _ =>
              KernelLogger.error(s"not supported preset: ${target.toolchain}")
          }
        }
        val toolchain = info._2(executor)
        val commands = toolchain.commands(task.getTaskType)
        CommandRunner.execute(commands, handler)
      })
  }

  def runTaskBackground(
      handler: ScaledaRunHandler,
      workingDir: File,
      target: TargetConfig,
      task: TaskConfig,
      daemon: Boolean = true
  ): Thread = {
    val t = new Thread(() => runTask(handler, workingDir, target, task))
    t.setDaemon(daemon)
    t.start()
    t
  }
}

trait ScaledaRunHandler {
  def onStdout(data: String): Unit

  def onStderr(data: String): Unit

  def onReturn(returnValue: Int): Unit

  def isTerminating: Boolean = false

  def onShellCommand(command: CommandDeps): Unit = {}

  def expectedReturnValue: Int = 0
}

trait ScaledaRunKernelHandlerWithReturn extends ScaledaRunHandler {
  override def onStdout(data: String): Unit = KernelLogger.info(data)

  override def onStderr(data: String): Unit = KernelLogger.error(data)
}

object ScaledaRunKernelHandler extends ScaledaRunKernelHandlerWithReturn {
  override def onStdout(data: String): Unit = KernelLogger.info(data)

  override def onStderr(data: String): Unit = KernelLogger.error(data)

  override def onReturn(returnValue: Int): Unit =
    KernelLogger.info(s"command done, returns ${returnValue}")
}
