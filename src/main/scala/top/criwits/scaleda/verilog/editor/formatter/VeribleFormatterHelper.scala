package top.criwits.scaleda
package verilog.editor.formatter

import top.criwits.scaleda.kernel.utils.{OS, Paths}

import java.io.File

object VeribleFormatterHelper {
  private val VERIBLE = "verible"
  private val WIN64 = "win64"
  private val LINUX64 = "lin64"
  private val FORMATTER = "verible-verilog-format"

  private val path = VERIBLE + "/" + (OS.getOSType match {
    case OS.Windows => WIN64 // TODO: 32 bit
    case OS.Unix => LINUX64
    case _ => "none" // FIXME
  }) + "/" + FORMATTER + (if (OS.isWindows) ".exe" else "")

  val veribleFormatter: File = new File(Paths.getBinaryDir, path)
}