package top.criwits.scaleda
package kernel.utils

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import top.criwits.scaleda.kernel.project.config.ProjectConfig
import top.criwits.scaleda.kernel.project.config.ProjectConfig.config
import top.criwits.scaleda.verilog.parser.{VerilogLexer, VerilogParser, VerilogParserBaseVisitor}

import java.io.{File, FileInputStream, FilenameFilter}
import java.util.regex.Pattern
import scala.collection.mutable.ListBuffer
import scala.io.Source

object KernelFileUtils {
  def getAllSourceFiles(sourceDir: File = new File(new File(ProjectConfig.projectBase.get).getAbsolutePath,
    ProjectConfig.config.source), suffixing: Set[String] = Set("v")): Seq[File] =
    sourceDir.listFiles(new FilenameFilter {
      override def accept(file: File, s: String) =
        suffixing.map(suffix => s.endsWith(s".${suffix}"))
          .reduceOption((a, b) => a || b).getOrElse(false)
    }).toList


  def getAllTestFiles(sourceDir: File = new File(new File(ProjectConfig.projectBase.get).getAbsolutePath,
    ProjectConfig.config.test), suffixing: Set[String] = Set("v")): Seq[File] =
    sourceDir.listFiles(new FilenameFilter {
      override def accept(file: File, s: String) =
        suffixing.map(suffix => s.endsWith(s".${suffix}"))
          .reduceOption((a, b) => a || b).getOrElse(false)
    }).toList

  def getAbsolutePath(path: String, projectBase: Option[String] = ProjectConfig.projectBase): Option[String] = {
    val file = new File(path)
    file.isAbsolute match {
      case true =>
        projectBase match {
          case Some(base) =>
            Some(new File(new File(base), path).getAbsolutePath)
          case None => None
        }
      case false => Some(file.getAbsolutePath)
    }
  }

  def getModuleTitle(verilogFile: File): Seq[String] = {
    val stream = new FileInputStream(verilogFile)
    val charStream = CharStreams.fromStream(stream)
    stream.close()
    val lexer = new VerilogLexer(charStream)
    val tokens = new CommonTokenStream(lexer)
    val parser = new VerilogParser(tokens)
    val tree = parser.source_text()

    class ModuleIdentifierVisitor extends VerilogParserBaseVisitor[String] {
      val title = new ListBuffer[String]

      override def visitModule_identifier(ctx: VerilogParser.Module_identifierContext): String = {
        val identifier = ctx.identifier()
        if (identifier != null) {
          val simpleIdentifier = identifier.Simple_identifier()
          if (simpleIdentifier != null) {
            val moduleName = simpleIdentifier.getText
            title += simpleIdentifier.getText
            moduleName
          } else null
        } else null
      }
    }

    val visitor = new ModuleIdentifierVisitor
    visitor.visit(tree)
    visitor.title.toSeq

//
//    val p = Pattern.compile("((.|\\n|\\r)*?)(module)(\\s)(\\w+)(\\s*)(((#)(\\s*)(\\((.|\\n|\\r)*?\\))(\\s*))?)(\\((.|\\n|\\r)*?\\))(\\s*)(;)((.|\\n|\\r)*?)(endmodule)((.|\\n|\\r)*?)")
//    val m = p.matcher(content)
//    if (m.find()) {
//      Some(m.group(5))
//    } else {
//      None
//    }
  }

  def getModuleFile(module: String): Option[File] = {
    getAllSourceFiles().foreach(f => {
      val matched = getModuleTitle(f).filter(_ == module)
      if (matched.nonEmpty) {
        return Some(f) // FIXME
      } else None
    })
    None
  }
}

