/*
 * Copyright 2019 helloscala.com
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

package fusion.json

import java.io.OutputStream
import java.util.TimeZone

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import fusion.json.jackson.Jackson
import fusion.shared.scalapb.json4s.Parser
import fusion.shared.scalapb.json4s.Printer
import org.json4s.JsonAST.JValue
import org.json4s.DefaultFormats
import org.json4s.Extraction
import org.json4s.Formats
import org.json4s.JsonInput
import org.json4s.Serialization
import org.json4s.jackson.JsonMethods
import scalapb.GeneratedMessage
import scalapb.GeneratedMessageCompanion
import scalapb.Message

import scala.reflect.Manifest

object Json4sMethods extends JsonMethods {
  override def mapper: ObjectMapper = Jackson.defaultObjectMapper
}

trait JsonUtils extends JsonMethods {
  override def mapper: ObjectMapper with ScalaObjectMapper = Jackson.defaultObjectMapper

  def toJsonString(in: JsonInput, useBigDecimalForDouble: Boolean = false, useBigIntForLong: Boolean = true): String =
    compact(parse(in))

  def fromJson[A](value: JValue)(implicit formats: Formats, mf: scala.reflect.Manifest[A]): A = {
    value.extract[A]
  }

  def fromJsonString[A](str: String)(implicit formats: Formats, mf: scala.reflect.Manifest[A]): A = {
    parse(str).extract[A]
  }

  object protobuf {
    val printer: Printer = new Printer().includingDefaultValueFields.formattingEnumsAsNumber.formattingLongAsNumber
    val parser: Parser = new Parser().ignoringUnknownFields

    def toJsonString[A <: GeneratedMessage](m: A): String = compact(toJson(m))

    def toJson[A <: GeneratedMessage](m: A): JValue = printer.toJson(m)

    @inline def parse[A <: GeneratedMessage](m: A): JValue = toJson(m)

    def fromJson[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](value: JValue): A =
      parser.fromJson(value)

    def fromJsonString[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](str: String): A =
      parser.fromJsonString(str)

    @inline def extract[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](value: JValue): A =
      fromJson(value)
  }

  object serialization extends Serialization {
    import java.io.Reader
    import java.io.Writer

    /** Serialize to String.
     */
    def write[A <: AnyRef](a: A)(implicit formats: Formats): String =
      mapper.writeValueAsString(Extraction.decompose(a)(formats))

    /** Serialize to Writer.
     */
    def write[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
      mapper.writeValue(out, Extraction.decompose(a)(formats))
      out
    }

    def write[A <: AnyRef](a: A, out: OutputStream)(implicit formats: Formats): Unit = {
      mapper.writeValue(out, Extraction.decompose(a)(formats: Formats))
    }

    /** Serialize to String (pretty format).
     */
    def writePretty[A <: AnyRef](a: A)(implicit formats: Formats): String =
      mapper.writerWithDefaultPrettyPrinter.writeValueAsString(Extraction.decompose(a)(formats))

    /** Serialize to Writer (pretty format).
     */
    def writePretty[A <: AnyRef, W <: Writer](a: A, out: W)(implicit formats: Formats): W = {
      mapper.writerWithDefaultPrettyPrinter.writeValue(out, Extraction.decompose(a)(formats))
      out
    }

    /** Deserialize from an JsonInput
     */
    def read[A](json: JsonInput)(implicit formats: Formats, mf: Manifest[A]): A =
      parse(json, formats.wantsBigDecimal, formats.wantsBigInt).extract(formats, mf)

    /** Deserialize from a Reader.
     */
    def read[A](in: Reader)(implicit formats: Formats, mf: Manifest[A]): A = {
      parse(in, formats.wantsBigDecimal, formats.wantsBigInt).extract(formats, mf)
    }
  }

  /** Default date format is UTC time.
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

object JsonUtils extends JsonUtils
