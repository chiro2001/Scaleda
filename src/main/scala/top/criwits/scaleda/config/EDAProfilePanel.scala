package top.criwits.scaleda
package config

import com.intellij.openapi.ui.Splitter
import com.intellij.ui.{AnActionButton, AnActionButtonRunnable, ColoredListCellRenderer, ToolbarDecorator}
import com.intellij.ui.components.JBList
import com.intellij.util.ui.JBUI
import top.criwits.scaleda.toolchain.ToolchainProfile

import java.awt.BorderLayout
import javax.swing.{DefaultListModel, JList, JPanel}
import scala.annotation.nowarn

class EDAProfilePanel extends JPanel(new BorderLayout) {
  // Right side, setting panel
  private val mySettingsPanel = new EDASettingsPanel

  // Left side, list
  private val myListModel = new MyListModel
  private val myList = new JBList[ToolchainProfile](myListModel)

  // Storage
  private def profiles = EDAProfileState.getInstance.profiles

  // Init panel
  initPanel()
  private def initPanel(): Unit = {
    val splitter = new Splitter(false, 0.3f)
    add(splitter, BorderLayout.CENTER)

    @nowarn("cat=deprecation")
    val listPanel = ToolbarDecorator.createDecorator(myList)
      .setAddAction((_: AnActionButton) => addProfile())
      .createPanel()
    splitter.setFirstComponent(listPanel)

    myList.setCellRenderer(new MyCellRenderer)

    val settingsComponent = mySettingsPanel.getComponent
    settingsComponent.setBorder(JBUI.Borders.emptyLeft(6))
    splitter.setSecondComponent(settingsComponent)
  }

  private def reloadItems(): Unit = {
    myListModel.clear()
    profiles.zipWithIndex.foreach(m => myListModel.add(m._2, m._1))
  }

  private def addProfile(): Unit = {

  }

  private class MyListModel extends DefaultListModel[ToolchainProfile] {

  }

  private class MyCellRenderer extends ColoredListCellRenderer[ToolchainProfile] {
    override def customizeCellRenderer(list: JList[_ <: ToolchainProfile], value: ToolchainProfile, index: Int, selected: Boolean,
                                       hasFocus: Boolean): Unit = {
      append(value.name)
    }

  }

}