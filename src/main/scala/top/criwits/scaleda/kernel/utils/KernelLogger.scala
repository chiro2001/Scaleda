package top.criwits.scaleda
package kernel.utils

import com.typesafe.scalalogging.Logger

class KernelLogger

object KernelLogger extends AbstractLogger {
  val logger: Logger = Logger("Kernel")

  //noinspection DuplicatedCode
  override def log(msg: String, level: LogLevel.Value): Unit = {
    import LogLevel._
    level match {
      case Debug => logger.debug(msg)
      case Info => logger.info(msg)
      case Warn => logger.warn(msg)
      case _ => logger.error(msg)
    }
  }
}
