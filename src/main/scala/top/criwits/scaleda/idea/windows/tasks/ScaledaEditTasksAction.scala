package top.criwits.scaleda
package idea.windows.tasks

import idea.ScaledaBundle
import kernel.project.config.ProjectConfig

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}

class ScaledaEditTasksAction
    extends AnAction(
      ScaledaBundle.message("windows.edit.tasks"),
      ScaledaBundle.message("windows.edit.tasks"),
      AllIcons.Actions.Edit
    ) {

  override def update(e: AnActionEvent): Unit = {
    val presentation = e.getPresentation
    presentation.setEnabled(ProjectConfig.projectBase.isDefined)
  }
  override def actionPerformed(e: AnActionEvent): Unit = {
    val _r = new ScaledaEditTasksDialog(e.getProject).showAndGet()
  }
}
