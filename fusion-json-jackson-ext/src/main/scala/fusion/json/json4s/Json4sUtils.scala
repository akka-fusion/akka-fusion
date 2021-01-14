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

package fusion.json.json4s

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.json.jackson.JacksonObjectMapperExtension
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Extraction, Formats, JsonInput, Serialization}

import java.io.OutputStream
import java.util.TimeZone
import scala.reflect.Manifest

class Json4sUtils(system: ExtendedActorSystem) extends JsonMethods with Extension {
  override def mapper: ObjectMapper = JacksonObjectMapperExtension(system).objectMapperJson

  def toJsonString(in: JsonInput): String =
    compact(parse(in))

  def fromJson[A](value: JValue)(implicit formats: Formats, mf: scala.reflect.Manifest[A]): A = {
    value.extract[A]
  }

  def fromJsonString[A](str: String)(implicit formats: Formats, mf: scala.reflect.Manifest[A]): A = {
    parse(str).extract[A]
  }

  object serialization extends Serialization {
    import java.io.{Reader, Writer}

    /**
     * Serialize to String.
     */
    def write[A <: AnyRef](a: A)(implicit formats: Formats): String =
      mapper.writeValueAsString(Extraction.decompose(a)(formats))

    /**
     * Serialize to Writer.
     */
    def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
      mapper.writeValue(out, Extraction.decompose(a)(formats))
      out
    }

    def write[A <: AnyRef](a: A, out: OutputStream)(implicit formats: Formats): Unit = {
      mapper.writeValue(out, Extraction.decompose(a)(formats: Formats))
    }

    /**
     * Serialize to String (pretty format).
     */
    def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String =
      mapper.writerWithDefaultPrettyPrinter.writeValueAsString(Extraction.decompose(a)(formats))

    /**
     * Serialize to Writer (pretty format).
     */
    def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
      mapper.writerWithDefaultPrettyPrinter.writeValue(out, Extraction.decompose(a)(formats))
      out
    }

    /**
     * Deserialize from an JsonInput
     */
    def read[A](json: JsonInput)(implicit formats: Formats, mf: Manifest[A]): A =
      parse(json, formats.wantsBigDecimal, formats.wantsBigInt).extract(formats, mf)

    /**
     * Deserialize from a Reader.
     */
    def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A = {
      parse(in, formats.wantsBigDecimal, formats.wantsBigInt).extract(formats, mf)
    }
  }

  /**
   * Default date format is UTC time.
   */
  implicit object defaultFormats extends DefaultFormats {
    private val UTC = TimeZone.getTimeZone("UTC")

    val losslessDate: ThreadLocal[java.text.SimpleDateFormat] = ThreadLocal.withInitial(() => {
      val f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      f.setTimeZone(UTC)
      f
    })
  }
}

object Json4sUtils extends ExtensionId[Json4sUtils] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): Json4sUtils = new Json4sUtils(system)

  override def lookup: ExtensionId[_ <: Extension] = Json4sUtils
}
