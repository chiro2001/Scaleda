package top.criwits.scaleda
package kernel.project.config

import idea.windows.tasks.ip.IPInstance
import kernel.utils.KernelFileUtils
import kernel.utils.KernelFileUtils.parseAsAbsolutePath

import com.fasterxml.jackson.annotation.JsonIgnore

import java.io.File
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

abstract class ConfigNode() {
  @JsonIgnore
  val topModule: Option[String]
  @JsonIgnore
  val constraints: Option[String]
  @JsonIgnore
  var parentNode: Option[ConfigNode] = None
  @JsonIgnore
  val source: String
  @JsonIgnore
  val sources: Seq[String]
  @JsonIgnore
  val test: String
  @JsonIgnore
  val tests: Seq[String]
  @JsonIgnore
  val ipFiles: Seq[String]
  @JsonIgnore
  val ipPaths: Seq[String]
  @JsonIgnore
  val ips: Seq[IPInstance]

  /** Get top module name
    * @return top module name, may not exist
    */
  @JsonIgnore
  def findTopModule: Option[String] = topModule match {
    case None =>
      parentNode match {
        case Some(parent) => parent.findTopModule
        case None         => None
      }
    case Some(str) => Some(str)
  }

  /** Get constraints file or directories
    * @return constraints file or directories
    */
  @JsonIgnore
  def getConstraints: Option[String] = constraints match {
    case Some(str) => Some(str)
    case None      => parentNode.flatMap(_.getConstraints)
  }

  /** Get all source set. Default is project base.
    * @return sources in absolute path
    */
  @JsonIgnore
  def getSourceSet(projectBase: Option[String] = None): Set[String] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    (parentNode match {
      case Some(parent) => parent.getSourceSet(projectBase = base)
      case None         => Set()
    }) ++ (if (source.nonEmpty) Set(parseAsAbsolutePath(source, projectBase = base)) else Set()) ++
      sources.filter(_.nonEmpty).map(parseAsAbsolutePath(_, projectBase = base))
  }

  /** Get testbench source set. Default is project base.
    * @return testbench in absolute path
    */
  @JsonIgnore
  def getTestSet(projectBase: Option[String] = None): Set[String] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    (parentNode match {
      case Some(parent) => parent.getTestSet(projectBase = base)
      case None         => Set()
    }) ++ (if (test.nonEmpty) Set(parseAsAbsolutePath(test, projectBase = base)) else Set()) ++
      tests.filter(_.nonEmpty).map(parseAsAbsolutePath(_, projectBase = base)) ++
      getSourceSet(projectBase = base)
  }

  /** Get all Simple Target IP files
    * @return simple target ip files or search path in absolute path
    */
  @JsonIgnore
  def getIpFiles(projectBase: Option[String] = None): Set[String] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    (parentNode match {
      case Some(parent) => parent.getIpFiles(projectBase = base)
      case None         => Set()
    }) ++ ipFiles.filter(_.nonEmpty).map(parseAsAbsolutePath(_, projectBase = base))
  }

  /** Get Scaleda IP search path, including basic paths: .ip, ip, ips
    * @return ip search paths
    */
  @JsonIgnore
  def getIpPaths(projectBase: Option[String] = None): Set[String] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    val basicPaths = ProjectConfig.projectIpPaths(projectBase = base) ++ ProjectConfig.libraryIpPaths
    basicPaths.map(_.getAbsolutePath) ++ (parentNode match {
      case Some(parent) => parent.getIpPaths(projectBase = base)
      case None         => Set()
    }) ++ ipPaths.filter(_.nonEmpty).map(parseAsAbsolutePath(_, projectBase = base))
  }

  /** Get defined Scaleda IP in this project, but not recursively from other IPs
    * @return map of ip abs-path and [[ProjectConfig]]
    */
  @JsonIgnore
  def getLocalIps(projectBase: Option[String] = None): Map[String, ProjectConfig] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    val paths = getIpPaths(projectBase = base)
    // search just one layer: .ips/<ip name>
    paths
      .map(new File(_))
      .filter(_.exists())
      .filter(_.isDirectory)
      .flatMap(p => KernelFileUtils.parseIpParentDirectory(p))
      .toMap
  }

  /** Recursively get ALL IPs from this project
    * @return map of ip abs-path and [[ProjectConfig]]
    */
  @JsonIgnore
  def getAllIps(projectBase: Option[String] = None): Map[String, ProjectConfig] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    // base of this current project
    val localIps = getLocalIps(projectBase = base)
    // BFS Search
    val q = mutable.Queue.empty[(String, ProjectConfig)]
    q ++= localIps
    // identifier is IP name, to avoid ring dependence
    val resultIpIds = new mutable.HashSet[String]()
    val results       = ArrayBuffer[(String, ProjectConfig)]()
    while (q.nonEmpty) {
      val top = q.dequeue()
      if (!resultIpIds.contains(top._2.exports.get.id)) {
        resultIpIds += top._2.exports.get.id
        results += top
        // only search more ips in ProjectConfig
        q ++= top._2.getLocalIps(projectBase = Some(top._1))
      }
    }
    results.toMap
  }

  /** Get IP Instances
    * @return name and context
    */
  @JsonIgnore
  def getIpInstances(projectBase: Option[String] = None): Seq[IPInstance] = {
    val base = if (projectBase.isEmpty) ProjectConfig.projectBase else projectBase
    (parentNode match {
      case Some(parent) => parent.getIpInstances(projectBase = base)
      case None         => Seq()
    }) ++ ips.map(i =>
      new IPInstance(
        module = i.module,
        typeId = i.typeId,
        options = if (i.options != null && i.options.nonEmpty) i.options else mutable.Map()
      )
    )
  }

  // TODO: get ip instances recursively
  // @JsonIgnore
  // def getAllIpInstances(projectBase: Option[String] = None): Map[String, Map[String, Any]] = {
  // }
}
