package top.criwits.scaleda
package kernel.project.ip

import kernel.project.config.ProjectConfig
import kernel.template.Template
import kernel.utils.{ImplicitPathReplace, NoPathReplace, RegexReplace}

import org.apache.commons.io.IOUtils

import java.io.{File, FileInputStream}

case class ExportConfig(
    // identifier
    id: String,
    // user friendly name
    name: String,
    module: String,
    stub: String = "",
    vendor: String = "",
    supports: Map[String, Map[String, String]] = Map(),
    options: Array[ExportOption] = Array(),
    actions: Map[String, ExportAction] = Map(),
    defines: Array[ExportDefineConfig] = Array(),
    templates: Seq[String] = Seq()
) {
  def getDefineHeadCode: String = {
    defines
      .map { define =>
        s"`define ${define.define} ${define.value}"
      }
      .mkString("\n")
  }

  def getOptionContextMap(data: Map[String, Any] = Map()): Map[String, Any] =
    options.map(t => t.name -> t.default).toMap ++ data

  def getModuleContextMap: Map[String, Any] = Map(
    "module" -> module,
    "name"   -> name
  )

  /** get context map, merge from default values
    * @param context instance options
    * @return context map
    */
  def getContextMap(context: Map[String, Any] = Map()): Map[String, Any] =
    getModuleContextMap ++ getOptionContextMap(context)

  /** Render stub text
    * @param context instance options
    * @param replace path replacer
    * @return rendered stub text
    */
  def renderStub(context: Map[String, Any] = Map())(implicit
      replace: ImplicitPathReplace = NoPathReplace
  ) = {
    val contextUse = getContextMap(context)
    Template.render(stub, contextUse)
  }

  /** render all templates
    * @param context instance options
    * @param projectBase project base path
    * @param replace path replacer
    * @return map of target filename and rendered content
    */
  def renderTemplate(context: Map[String, Any] = Map(), projectBase: Option[String] = None)(implicit
      replace: ImplicitPathReplace = NoPathReplace
  ): Map[String, String] = {
    val base = if (projectBase.nonEmpty) projectBase.get else ProjectConfig.projectBase.get
    // relative path from project base
    templates
      .map(name => (name, new File(base, name)))
      .filter(_._2.exists())
      .filter(_._2.isFile)
      .map(f => {
        val (name, file)   = f
        val content        = IOUtils.toString(new FileInputStream(file), "UTF-8")
        val replacer       = new RegexReplace("(.*)\\.j2", Seq("$1"))
        val targetFilename = replacer.doRegexReplace(name)
        (targetFilename, Template.render(content, getContextMap(context)))
      })
      .toMap
  }
}
