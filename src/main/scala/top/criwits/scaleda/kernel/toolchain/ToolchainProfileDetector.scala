package top.criwits.scaleda
package kernel.toolchain

import scala.async.Async.{async, await}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait ToolchainProfileDetector {
  def detectProfiles: Future[Seq[ToolchainProfile]] = async { Seq() }
}

object ToolchainProfileDetector {
  private val allDetectors = ArrayBuffer[ToolchainProfileDetector]()
  private var detected: Option[Seq[ToolchainProfile]] = None

  def registerDetector(detector: ToolchainProfileDetector): Unit =
    allDetectors.addOne(detector)

  def detectProfiles(cached: Boolean = true): Future[Seq[ToolchainProfile]] =
    async {
      if (cached && detected.nonEmpty) detected.get
      else {
        val detectors = allDetectors.toSeq
        if (detectors.nonEmpty) {
          val profileTest = await(detectors.head.detectProfiles)
          println(profileTest)
        }
        val futures = detectors.map(d => d.detectProfiles)
        // var result: Seq[Seq[ToolchainProfile]] = Seq()
        // val result = (for { s <- futures } yield await(s)).flatten
        // for (s <- futures) { await(s) }
        // for {s <- futures} yield await(s)
        val profiles = await(for {
          checked <- Future.sequence(futures)
        } yield checked.flatten)
        detected = Some(profiles)
        profiles
      }
    }
}
