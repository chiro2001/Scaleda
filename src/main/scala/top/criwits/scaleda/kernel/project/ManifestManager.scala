package top.criwits.scaleda
package kernel.project

import scala.collection.mutable

object ManifestManager {
  // map of project abs-path to project basic info, for default cli project, key is "", value.project is None
  private val manifestPool: mutable.HashMap[String, ProjectManifest] = mutable.HashMap(
    "" -> new ProjectManifest()
  )
  def putManifest(key: String, manifest: ProjectManifest, overrideManifest: Boolean = false): Unit = {
    if (overrideManifest || !manifestPool.contains(key))
      manifestPool.put(key, manifest)
  }
  def setDefaultManifest(manifest: ProjectManifest) = {
    this.manifestPool.put("", manifest)
  }
  def removeManifest(project: AbstractProject): Unit = {
    manifestPool.remove(project.getBasePath)
  }
  def getOptionalManifest(project: AbstractProject = AbstractProject.default): Option[ProjectManifest] = {
    val p = if (project == null) AbstractProject.default else project
    manifestPool.get(p.getBasePath)
  }
  def getManifest(project: AbstractProject = AbstractProject.default): ProjectManifest = {
    val p   = if (project == null) AbstractProject.default else project
    val key = p.getBasePath
    if (manifestPool.contains(key)) manifestPool(key)
    else {
      val v = new ProjectManifest(p)
      manifestPool.put(key, v)
      v
    }
  }

  private val objectPool: mutable.HashMap[ProjectManifest, mutable.HashMap[String, Any]] = mutable.HashMap()

  def getObjectPool = objectPool

  def getObject[T](key: String)(implicit project: AbstractProject = AbstractProject.default): Option[T] = {
    val p = getManifest(project)
    if (getObjectPool.contains(p) && getObjectPool(p).contains(key)) Some(getObjectPool(p)(key).asInstanceOf[T])
    else None
  }

  def putObject[T](key: String, value: T)(implicit project: AbstractProject = AbstractProject.default): Unit = {
    val p = getManifest(project)
    if (!getObjectPool.contains(p)) getObjectPool.put(p, mutable.HashMap())
    getObjectPool(p).put(key, value)
  }
}
