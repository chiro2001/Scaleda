package top.criwits.scaleda
package lang

import com.intellij.openapi.fileTypes.{FileType, LanguageFileType}
import com.intellij.openapi.util.IconLoader


import javax.swing.Icon

final class VerilogFileType extends LanguageFileType(VerilogLanguage){
  override def getName: String = "Verilog"
  override def getDescription: String = "Verilog"
  override def getDefaultExtension: String = VerilogFileType.DefaultExtension
  override def getIcon: Icon = VerilogFileType.DefaultIcon
}

object VerilogFileType {
  final val DefaultExtension = "v"
  final val DefaultIcon = IconLoader.getIcon("/icons/verilog.svg", VerilogFileType.getClass)

  def isVerilog(fileType: FileType): Boolean = fileType match {
    case _: VerilogFileType => true
    case _ => false
  }
}