package top.criwits.scaleda
package kernel.shell

import kernel.project.config.ProjectConfig
import kernel.project.task.{TargetConfig, TaskConfig, TaskType}
import kernel.shell.command.CommandRunner
import kernel.toolchain.Toolchain
import kernel.toolchain.executor.{SimulationExecutor, SynthesisExecutor}
import kernel.toolchain.impl.Vivado
import kernel.utils.KernelLogger

import java.io.File

object ScaledaRun {
  def runTask(
      handler: ScaledaRunHandler,
      workingDir: File,
      task: TargetConfig,
      target: TaskConfig
  ) = {
    KernelLogger.info(s"runTask workingDir=${workingDir.getAbsoluteFile}")
    val config = ProjectConfig.getConfig().get
    val info = Toolchain.toolchains(task.toolchain)
    // find profile
    Toolchain
      .profiles()
      .filter(p => p.toolchainType == task.toolchain)
      .foreach(profile => {
        // generate executor
        val executor = target.getType match {
          case TaskType.Simulation =>
            SimulationExecutor(
              workingDir = new File(workingDir, ".sim"),
              topFile = new File(config.topFile),
              profile = profile
            )
          case TaskType.Synthesis =>
            SynthesisExecutor(
              workingDir = new File(workingDir, ".synth"),
              topFile = new File(config.topFile),
              profile = profile
            )
          case _ => {
            KernelLogger.error(s"unsupported task type: ${target.getType}")
            ???
          }
        }
        if (target.preset) {
          task.toolchain match {
            case Vivado.internalID => {
              val r = new Vivado.TemplateRenderer(
                executor = executor,
                taskConfig = task
              )
              r.render()
            }
            case _ =>
              KernelLogger.error(s"not supported preset: ${task.toolchain}")
          }
        }
        val toolchain = info._2(executor)
        val commands = toolchain.commands(target.getType)
        CommandRunner.execute(commands, handler)
      })
  }

  def runTaskBackground(
      handler: ScaledaRunHandler,
      workingDir: File,
      task: TargetConfig,
      target: TaskConfig,
      daemon: Boolean = true
  ): Thread = {
    val t = new Thread(() => runTask(handler, workingDir, task, target))
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
