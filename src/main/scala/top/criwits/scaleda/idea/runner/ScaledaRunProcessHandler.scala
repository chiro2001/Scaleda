package top.criwits.scaleda
package idea.runner

import idea.ScaledaBundle
import idea.utils.{MainLogger, OutputLogger}
import kernel.shell.ScaledaRunHandler
import kernel.shell.command.CommandDeps
import kernel.utils.BasicLogger

import com.intellij.execution.process.ProcessHandler
import com.intellij.openapi.project.Project

import java.io.OutputStream
import scala.collection.mutable.ArrayBuffer

/** Handle a task process locally or remotely
  * @param logger using logger
  * @param rt Runtime of this run
  * @param invokeAfterFinish will emit with [[ScaledaRuntimeInfo]] after `finishedAll`
  */
class ScaledaRunProcessHandler(
    project: Project,
    logger: BasicLogger,
    rt: ScaledaRuntimeInfo,
    invokeAfterFinish: (ScaledaRuntimeInfo, Seq[Int], Boolean, Boolean) => Unit
) extends ProcessHandler
    with ScaledaRunHandler {
  // Set terminating <- `true` to invoke stopping
  var terminating = false
  // terminated will be set `true` after process really terminated
  var terminated = false

  val returnValues = ArrayBuffer[Int]()

  /** Called when destroy button clicked
    */
  override def destroyProcessImpl(): Unit = {
    MainLogger.warn(
      s"destroyProcessImpl, running: $terminated, stopping: $terminating"
    )
    terminating = true
    notifyProcessTerminated(returnValues.headOption.getOrElse(0))
  }

  override def detachProcessImpl(): Unit = {
    MainLogger.warn("detachProcessImpl")
    notifyProcessDetached()
  }

  override def detachIsDefault(): Boolean = false

  override def isProcessTerminated: Boolean = terminated

  override def isProcessTerminating: Boolean = terminating

  // Not in use
  private val outputStream = new OutputStream {
    override def write(i: Int): Unit = MainLogger.warn("getProcessInput:", i)
  }
  override def getProcessInput: OutputStream = outputStream

  override def onShellCommand(command: CommandDeps) =
    logger.debug("cd", s"\"${command.path}\"", "&&", command.args.map(s => s"\"$s\"").mkString(" "))

  private val outputLogger = OutputLogger(project)

  override def onStepDescription(data: String): Unit = logger.debug(data)

  override def onStdout(data: String): Unit = {
    logger.info(data)
    outputLogger.info(data)
  }

  override def onStderr(data: String): Unit = {
    logger.warn(data)
    outputLogger.warn(data)
  }

  override def onReturn(returnValue: Int, finishedAll: Boolean, meetErrors: Boolean): Unit = {
    val msg = ScaledaBundle.message("task.run.return.text", returnValue)
    if (meetErrors) {
      logger.warn(msg)
      outputLogger.warn(msg)
    }
    else {
      logger.debug(msg)
      outputLogger.debug(msg)
    }
    returnValues.append(returnValue)
    if (finishedAll || meetErrors) {
      terminating = false
      terminated = true
      // invoke only success all
      invokeAfterFinish(rt, returnValues.toSeq, finishedAll, meetErrors)
    }
  }

  override def isTerminating: Boolean = terminating
}
