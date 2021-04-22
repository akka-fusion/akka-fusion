/*
 * Copyright 2019-2021 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.security.util

import java.io.InputStream
import java.net.URL
import java.util.TimeZone

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 09:27:53
 */
object JsonUtils {

  val objectMapper: ObjectMapper = new ObjectMapper()
    .findAndRegisterModules()
    .setTimeZone(TimeZone.getTimeZone("Asia/Chongqing"))
    //            .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
    .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
    .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
    .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, false)
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    .configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false)
    .configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    .configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS, true)
    .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
    .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)

  val objectMapperNotNull: ObjectMapper = objectMapper.copy().setSerializationInclusion(JsonInclude.Include.NON_NULL)

  def writeValueAsBytes(value: Any): Array[Byte] = {
    value match {
      case str: String =>
        val node: JsonNode = readTree(str)
        objectMapper.writeValueAsBytes(node)
      case _ =>
        objectMapper.writeValueAsBytes(value)
    }
  }

  def writeValueAsString(value: Any): String = {
    value match {
      case text: String =>
        val node: JsonNode = readTree(text)
        objectMapper.writeValueAsString(node)
      case _ =>
        objectMapper.writeValueAsString(value)
    }
  }

  def readValue[T](value: String, valueType: Class[T]): T = objectMapper.readValue(value, valueType)

  def readValue[T](value: Array[Byte], valueType: Class[T]): T = objectMapper.readValue(value, valueType)

  def readValue[T](text: String, typeReference: TypeReference[T]): T = objectMapper.readValue(text, typeReference)

  def readTree(text: String): JsonNode = objectMapper.readTree(text)

  def treeToValue[T](node: TreeNode, valueType: Class[T]): T = objectMapper.treeToValue(node, valueType)

  def readTree(bytes: Array[Byte]): JsonNode = objectMapper.readTree(bytes)

  def readTree(in: InputStream): JsonNode = objectMapper.readTree(in)

  def readObjectNode(text: String): ObjectNode = return readTree(text).asInstanceOf[ObjectNode]

  def readArrayNode(text: String): ArrayNode = readTree(text).asInstanceOf[ArrayNode]

  def readTree(in: URL): JsonNode = objectMapper.readTree(in)

  def createObjectNode: ObjectNode = objectMapper.createObjectNode

  def createArrayNode: ArrayNode = objectMapper.createArrayNode

  def valueToTree(obj: Any): JsonNode = objectMapper.valueToTree(obj)

  def stringify(data: Any): String = writeValueAsString(data)

  def getString(node: JsonNode): Option[String] = Option(node).map(_.asText())

  def getString(node: JsonNode, deft: String): String = getString(node).getOrElse(deft)

  def getLong(node: JsonNode): Option[Long] = Option(node).map(_.asLong())

  def getLong(node: JsonNode, deft: Long): Long = getLong(node).getOrElse(deft)

  def getInt(node: JsonNode): Option[Int] = Option(node).map(_.asInt())

  def getInt(node: JsonNode, deft: Int): Int = getInt(node).getOrElse(deft)
}
