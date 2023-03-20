package top.criwits.scaleda
package idea.windows.tasks.project

import idea.windows.tasks.{ScaledaEditPanelWrapper, ScaledaRunRootNode}

import com.intellij.icons.AllIcons
import com.intellij.ui.DocumentAdapter
import idea.ScaledaBundle

import top.criwits.scaleda.kernel.utils.KernelFileUtils

import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import scala.collection.mutable.ListBuffer

class ScaledaEditProjectPanelWrapper(val projectConfig: ScaledaRunRootNode) extends ScaledaEditPanelWrapper {
  val inner = new ScaledaEditProjectPanel

  // initalise values
  inner.projectNameField.setText(projectConfig.name)
  inner.sourceField.setText(projectConfig.source)
  inner.testField.setText(projectConfig.test)
  inner.topModuleField.setText(projectConfig.topModule.getOrElse(""))
  inner.constraintsField.setText(projectConfig.constraints.getOrElse(""))

  def updateValue(): Unit = {
    projectConfig.name = inner.projectNameField.getText
    projectConfig.source = inner.sourceField.getText
    projectConfig.test = inner.testField.getText
    projectConfig.topModule = if (inner.topModuleField.getText.nonEmpty) Some(inner.topModuleField.getText) else None
    projectConfig.constraints = if (inner.constraintsField.getText.nonEmpty) Some(inner.constraintsField.getText) else None
    checkValue
  }

  def checkValue: Boolean = {
    inner.statusLabel.clear()
    val messages = new ListBuffer[String]
    if (!KernelFileUtils.isLegalName(projectConfig.name)) messages.addOne(ScaledaBundle.message("windows.edit.project.illegal.name"))
    if (projectConfig.source.isEmpty /* todo: check exist */) messages.addOne(ScaledaBundle.message("windows.edit.project.need.src"))
    if (projectConfig.test.isEmpty /* todo: above */) messages.addOne(ScaledaBundle.message("windows.edit.project.need.test"))

    if (messages.nonEmpty) {
      inner.statusLabel.setIcon(AllIcons.General.BalloonError)
      inner.statusLabel.append(messages.mkString("; "))
      false
    } else {
      inner.statusLabel.setIcon(AllIcons.General.InspectionsOK)
      inner.statusLabel.append(ScaledaBundle.message("windows.edit.ok"))
      true
    }
  }

  checkValue

  inner.projectNameField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = updateValue()
  })

  inner.sourceField.getTextField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = updateValue()
  })
  inner.testField.getTextField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = updateValue()
  })
  inner.topModuleField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = updateValue()
  })
  inner.constraintsField.getTextField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = updateValue()
  })

  def getPanel: JPanel = inner.mainPanel
}
