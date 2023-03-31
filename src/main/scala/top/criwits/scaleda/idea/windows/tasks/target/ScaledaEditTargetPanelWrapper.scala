package top.criwits.scaleda
package idea.windows.tasks.target

import kernel.project.config.{ProjectConfig, TargetConfig}
import kernel.toolchain.Toolchain

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.Splitter
import com.intellij.ui.{DocumentAdapter, SimpleColoredComponent}
import com.intellij.util.ui.JBUI
import org.jdesktop.swingx.prompt.PromptSupport
import idea.ScaledaBundle
import idea.utils.MainLogger
import idea.windows.tasks.{ScaledaEditPanelWrapper, ScaledaRunTargetNode}
import idea.windows.tasks.target.toolchain.{EmptyConfigPanel, ExtraConfigPanel, VivadoConfigPanel}

import top.criwits.scaleda.kernel.utils.KernelFileUtils

import java.awt.{BorderLayout, Color, Font}
import java.awt.event.{ItemEvent, ItemListener}
import javax.swing.JPanel
import javax.swing.event.DocumentEvent
import scala.collection.mutable.ListBuffer

class ScaledaEditTargetPanelWrapper(val targetConfig: ScaledaRunTargetNode, setValid: => Unit) extends JPanel with ScaledaEditPanelWrapper {
  val inner = new TargetBasicPanel

  setLayout(new BorderLayout())
  add(inner.mainPanel, BorderLayout.NORTH)

  /** LOWER PANEL */
  private val lowerPanel = new JPanel()
  lowerPanel.setLayout(new BorderLayout())
  add(lowerPanel, BorderLayout.CENTER)

  private val statusLabel = new SimpleColoredComponent
  add(statusLabel, BorderLayout.SOUTH)

  private val toolchainList = Toolchain.toolchains.toList
  toolchainList.foreach(f => inner.toolchainField.addItem(f._2._1))
  inner.toolchainField.setSelectedIndex(0) // should work??

  inner.nameField.setText(targetConfig.name)
  inner.topModuleField.setText(targetConfig.topModule.getOrElse(""))
  inner.constraintsField.setText(targetConfig.constraints.getOrElse(""))
  inner.toolchainField.setSelectedIndex(Toolchain.toolchains.zipWithIndex.filter(_._1._1 == targetConfig.toolchain).head._2) // FIXME: if not exist?

  def getPanel: JPanel = this

  // listeners
  inner.nameField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = {
      targetConfig.name = inner.nameField.getText
      checkValue
    }
  })

  inner.topModuleField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = {
      targetConfig.topModule = if (inner.topModuleField.getText.nonEmpty) Some(inner.topModuleField.getText) else None
      checkValue
    }
  })

  inner.toolchainField.addItemListener((e: ItemEvent) => if (e.getStateChange == ItemEvent.SELECTED) {
    targetConfig.toolchain = toolchainList(inner.toolchainField.getSelectedIndex)._1
    updateConfigPanel()
    checkValue
  })

  inner.constraintsField.getTextField.getDocument.addDocumentListener(new DocumentAdapter {
    override def textChanged(e: DocumentEvent): Unit = {
      targetConfig.constraints = if (inner.constraintsField.getText.nonEmpty) Some(inner.constraintsField.getText) else None
      checkValue
    }
  })


  private var configPanel: ExtraConfigPanel = new EmptyConfigPanel

  private def updateConfigPanel(): Unit = {
    configPanel = targetConfig.toolchain match {
      case "vivado" => new VivadoConfigPanel
      case _ => new EmptyConfigPanel
    }
    configPanel.addEditListener(new DocumentAdapter {
      override def textChanged(e: DocumentEvent): Unit = {
        targetConfig.options = configPanel.getTargetSpecificConfig
        checkValue
      }
    })
    lowerPanel.removeAll()
    lowerPanel.add(configPanel.getComponent, BorderLayout.CENTER)
    configPanel.loadTargetSpecificConfig(targetConfig.options.getOrElse(Map()))
  }

  updateConfigPanel()
//
//  // check top module status
//  private val projectTopModuleValid = targetConfig.topModule.isDefined
//  if (projectTopModuleValid) {
//    // add a prompt!
//    PromptSupport.setPrompt(ScaledaBundle.message("windows.edit.target.inherited_top_module",
//      projectConfig.topModule.get),
//      inner.topModuleField)
//    PromptSupport.setFontStyle(Font.ITALIC, inner.topModuleField)
//    PromptSupport.setBackground(new Color(0, 0, 0, 0), inner.topModuleField)
//  }


  override def checkValue: Boolean = {
    setValid
    statusLabel.clear()
    val messages = new ListBuffer[String]
    if (!KernelFileUtils.isLegalName(targetConfig.name)) messages.addOne(ScaledaBundle.message("windows.edit.target.illegal.name"))
    if (!configPanel.getStatus) messages.addOne(ScaledaBundle.message("windows.edit.target.invalid.options"))

    if (messages.nonEmpty) {
      statusLabel.setIcon(AllIcons.General.BalloonError)
      statusLabel.append(messages.mkString("; "))
      false
    } else {
      statusLabel.setIcon(AllIcons.General.InspectionsOK)
      statusLabel.append(ScaledaBundle.message("windows.edit.ok"))
      true
    }
  }

  checkValue
}