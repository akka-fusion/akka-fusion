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

package fusion.discoveryx.server.util

import java.lang.reflect.InvocationTargetException

import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentTypeRange, MediaType, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.stream.Materializer
import akka.util.ByteString
import org.json4s.MappingException
import org.json4s.jackson.JsonMethods
import scalapb.json4s.{ Parser, Printer }
import scalapb.{ GeneratedMessage, GeneratedMessageCompanion, Message }

import scala.concurrent.ExecutionContext

trait ProtobufJsonSupport {}

object ProtobufJsonSupport extends ProtobufJsonSupport {
  sealed abstract class ShouldWritePretty
  final object ShouldWritePretty {
    final object True extends ShouldWritePretty
    final object False extends ShouldWritePretty
  }

  def mediaTypes: Seq[MediaType.WithFixedCharset] = List(MediaTypes.`application/json`)

  def unmarshallerContentTypes: Seq[ContentTypeRange] = mediaTypes.map(ContentTypeRange.apply)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(unmarshallerContentTypes: _*).mapWithCharset {
      case (ByteString.empty, _) => throw Unmarshaller.NoContentException
      case (data, charset)       => data.decodeString(charset.nioCharset.name)
    }

  private val jsonStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(Marshaller.stringMarshaller)

  /**
   * HTTP entity => `A`
   *
   * @tparam A type to decode
   * @return unmarshaller for `A`
   */
  implicit def unmarshaller[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](
      implicit parser: Parser = ProtobufJson4s.parser): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(s => parser.fromJsonString(s)).recover(throwCause)

  /**
   * `A` => HTTP entity
   *
   * @tparam A type to encode, must be upper bounded by `AnyRef`
   * @return marshaller for any `A` value
   */
  implicit def marshaller[A <: GeneratedMessage](
      implicit printer: Printer = ProtobufJson4s.printer,
      shouldWritePretty: ShouldWritePretty = ShouldWritePretty.False): ToEntityMarshaller[A] =
    shouldWritePretty match {
      case ShouldWritePretty.False => jsonStringMarshaller.compose(a => JsonMethods.compact(printer.toJson(a)))
      case ShouldWritePretty.True  => jsonStringMarshaller.compose(a => JsonMethods.pretty(printer.toJson(a)))
    }

  private def throwCause[A](ec: ExecutionContext)(mat: Materializer): PartialFunction[Throwable, A] = {
    case MappingException(_, e: InvocationTargetException) => throw e.getCause
  }
}
