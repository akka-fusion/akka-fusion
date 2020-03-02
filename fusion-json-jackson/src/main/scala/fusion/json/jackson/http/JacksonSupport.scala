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

package fusion.json.jackson.http

import java.lang.reflect.{ ParameterizedType, Type => JType }

import akka.http.javadsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshal, Unmarshaller }
import akka.http.scaladsl.util.FastFuture
import akka.stream.scaladsl.{ Flow, Source }
import akka.util.ByteString
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper

import scala.collection.immutable.Seq
import scala.concurrent.Future
import scala.reflect.runtime.universe._
import scala.util.Try
import scala.util.control.NonFatal

/**
 * JSON marshalling/unmarshalling using an in-scope Jackson's ObjectMapper
 */
trait JacksonSupport {
  type SourceOf[A] = Source[A, _]

  implicit def objectMapper: ObjectMapper

  val mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(MediaTypes.`application/json`)

  val unmarshallerContentTypes: Seq[ContentTypeRange] =
    (mediaTypes :+ MediaTypes.`text/plain`).map(ContentTypeRange.apply)

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(unmarshallerContentTypes: _*).mapWithCharset {
      case (ByteString.empty, _) => throw Unmarshaller.NoContentException
      case (data, charset)       => data.decodeString(charset.nioCharset.name)
    }

  private def typeReference[T: TypeTag]: TypeReference[T] = {
    val t = typeTag[T]
    val mirror = t.mirror
    def mapType(t: Type): JType =
      if (t.typeArgs.isEmpty)
        mirror.runtimeClass(t)
      else
        new ParameterizedType {
          def getRawType = mirror.runtimeClass(t)

          def getActualTypeArguments = t.typeArgs.map(mapType).toArray

          def getOwnerType = null
        }

    new TypeReference[T] {
      override def getType = mapType(t.tpe)
    }
  }

  private def sourceByteStringMarshaller(
      mediaType: MediaType.WithFixedCharset): Marshaller[SourceOf[ByteString], MessageEntity] =
    Marshaller[SourceOf[ByteString], MessageEntity] { implicit ec => value =>
      try FastFuture.successful {
        Marshalling.WithFixedContentType(mediaType, () => HttpEntity(contentType = mediaType, data = value)) :: Nil
      } catch {
        case NonFatal(e) => FastFuture.failed(e)
      }
    }

  private val jsonSourceStringMarshaller =
    Marshaller.oneOf(mediaTypes: _*)(sourceByteStringMarshaller)

  private def jsonSource[A](entitySource: SourceOf[A])(
      implicit objectMapper: ObjectMapper,
      support: JsonEntityStreamingSupport): SourceOf[ByteString] =
    entitySource.map(elem => ByteString(objectMapper.writeValueAsBytes(elem))).via(support.framingRenderer)

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A](implicit ct: TypeTag[A], objectMapper: ObjectMapper): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map(data => objectMapper.readValue(data, typeReference[A]))

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[A](implicit objectMapper: ObjectMapper): ToEntityMarshaller[A] = {
    Marshaller.withFixedContentType(ContentTypes.`application/json`)(a =>
      HttpEntity(ContentTypes.`application/json`, objectMapper.writeValueAsString(a)))
  }

  /**
   * `ByteString` => `A`
   *
   * @tparam A type to decode
   * @return unmarshaller for any `A` value
   */
  implicit def fromByteStringUnmarshaller[A](
      implicit ct: TypeTag[A],
      objectMapper: ObjectMapper): Unmarshaller[ByteString, A] =
    Unmarshaller { _ => bs =>
      Future.fromTry(Try(objectMapper.readValue(bs.toArray, typeReference[A])))
    }

  /**
   * HTTP entity => `Source[A, _]`
   *
   * @tparam A type to decode
   * @return unmarshaller for `Source[A, _]`
   */
  implicit def sourceUnmarshaller[A](
      implicit ct: TypeTag[A],
      objectMapper: ObjectMapper,
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()): FromEntityUnmarshaller[SourceOf[A]] =
    Unmarshaller
      .withMaterializer[HttpEntity, SourceOf[A]] { implicit ec => implicit mat => entity =>
        def asyncParse(bs: ByteString) =
          Unmarshal(bs).to[A]

        def ordered =
          Flow[ByteString].mapAsync(support.parallelism)(asyncParse)

        def unordered =
          Flow[ByteString].mapAsyncUnordered(support.parallelism)(asyncParse)

        Future.successful {
          entity.dataBytes.via(support.framingDecoder).via(if (support.unordered) unordered else ordered)
        }
      }
      .forContentTypes(unmarshallerContentTypes: _*)

  /**
   * `SourceOf[A]` => HTTP entity
   *
   * @tparam A type to encode
   * @return marshaller for any `SourceOf[A]` value
   */
  implicit def sourceMarshaller[A](
      implicit ct: TypeTag[A],
      objectMapper: ObjectMapper,
      support: JsonEntityStreamingSupport = EntityStreamingSupport.json()): ToEntityMarshaller[SourceOf[A]] =
    jsonSourceStringMarshaller.compose(jsonSource[A])
}

class JacksonSupportImpl()(implicit override val objectMapper: ObjectMapper) extends JacksonSupport

//object JacksonSupport extends JacksonSupport
