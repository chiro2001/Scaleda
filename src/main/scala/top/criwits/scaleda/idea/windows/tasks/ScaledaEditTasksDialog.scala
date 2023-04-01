package top.criwits.scaleda
package idea.windows.tasks

import idea.ScaledaBundle
import idea.runner.task.ScaledaReloadTasksAction
import kernel.project.config.ProjectConfig

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper

import javax.swing.JComponent

class ScaledaEditTasksDialog(project: Project) extends DialogWrapper(project) {
  init()
  setTitle(ScaledaBundle.message("windows.edit"))
  setSize(800, 600)
  setResizable(true)

  var mainPanel: ScaledaEditTasksPanel = _

  override def createCenterPanel(): JComponent = {
    // get root node
    // TODO: if there're no project config?
    ProjectConfig
      .getConfig()
      .map(c => {
        val rootNode = new ScaledaRunRootNode(c)
        if (mainPanel == null) mainPanel = new ScaledaEditTasksPanel(rootNode, setValid)
        mainPanel
      }).orNull
  }
  override def doOKAction(): Unit = {
    ProjectConfig.saveConfig(mainPanel.scaledaRunRootNode.toProjectConfig)
    ActionManager.getInstance().tryToExecute(new ScaledaReloadTasksAction, null, null, null, true)
    super.doOKAction()
  }

  private def setValid(ok: Boolean) = {
    setOKActionEnabled(ok)
  }

}
