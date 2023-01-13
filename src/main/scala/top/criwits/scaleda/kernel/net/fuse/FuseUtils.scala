package top.criwits.scaleda
package kernel.net.fuse

import kernel.utils.KernelLogger

import ru.serce.jnrfuse.{FuseException, FuseStubFS}

import java.io.File
import java.nio.file.attribute.{PosixFileAttributes, PosixFilePermissions}
import java.nio.file.{Files, Paths}
import scala.sys.process._

object FuseUtils {
  def mountFs(
      fs: FuseStubFS,
      mountPoint: String,
      blocking: Boolean = true
  ): Unit = {
    try {
      KernelLogger.warn(s"mounting to $mountPoint")
      val path = Paths.get(mountPoint)
      val file = new File(mountPoint)
      if (!file.exists())
        KernelLogger.warn("creating dirs with returns:", file.mkdirs())
      try {
        fs.mount(path, blocking)
      } catch {
        case _: FuseException => {
          s"umount $mountPoint".!
          fs.mount(path, blocking)
        }
      }
    } catch {
      case e: InterruptedException =>
        KernelLogger.warn("mount interrupted cause:", e)
    } finally {
      fs.umount()
      KernelLogger.info(s"umount done")
    }
  }

  def fileAttrsUnixString(file: File): String = {
    val path = file.toPath
    val attrs = Files.readAttributes(path, classOf[PosixFileAttributes])
    val perms = attrs.permissions()
    PosixFilePermissions.toString(perms)
  }

  def fileAttrsUnixToInt(file: File): Int = {
    val str = fileAttrsUnixString(file)
    def bitToInt(c: Char): Int = if (c == '-') 0 else 1
    def groupToInt(group: String): Int =
      (0 until 3).map(i => bitToInt(group.charAt(i)) << (2 - i)).sum
    (0 until 3)
      .map(_ * 3)
      .map(i => groupToInt(str.slice(i, i + 3)) << (6 - i))
      .sum
  }
}