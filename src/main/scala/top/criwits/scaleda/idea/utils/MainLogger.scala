package top.criwits.scaleda
package idea.utils

import com.intellij.openapi.diagnostic.Logger

class MainLogger

object MainLogger {
  val logger: Logger = Logger.getInstance(classOf[MainLogger])

}
