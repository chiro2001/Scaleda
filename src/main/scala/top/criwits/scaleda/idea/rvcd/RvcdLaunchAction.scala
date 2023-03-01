package top.criwits.scaleda
package idea.rvcd

import idea.utils.OutputLogger
import kernel.rvcd.RvcdClient
import kernel.shell.command.{CommandDeps, CommandRunner}

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import rvcd.rvcd.RvcdOpenFileWith

class RvcdLaunchAction extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    new Thread(() => {
      CommandRunner.execute(
        Seq(
          CommandDeps(
            commands = Seq(RvcdService.rvcdFile.getAbsolutePath),
            description = "Start RVCD Instance"
          )
        ),
        new OutputLogger.StdErrToWarningHandler(event.getProject)
      )
    }).start()
  }
}

class RvcdLaunchActionWith(request: RvcdOpenFileWith) extends AnAction {
  override def actionPerformed(event: AnActionEvent): Unit = {
    val (client, shutdown) = RvcdClient()
    client.openFileWith(request)
    shutdown()
  }
}
