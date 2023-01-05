package top.criwits.scaleda
package kernel.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import java.io.{BufferedWriter, File, FileWriter}

object JsonHelper extends AbstractSerializeHelper {
  private val mapper = JsonMapper.builder()
    .addModule(DefaultScalaModule)
    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .build()

  def apply[T](file: File, clazz: Class[T]): T = mapper.readValue[T](file, clazz)

  def apply(value: Any): String = mapper.writeValueAsString(value)

  def apply(value: Any, file: File): Unit = {
    val writer = new BufferedWriter(new FileWriter(file))
    writer.write(apply(value))
    writer.close()
  }
}
