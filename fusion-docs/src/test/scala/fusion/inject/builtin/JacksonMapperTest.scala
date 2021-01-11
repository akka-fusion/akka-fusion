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

package fusion.inject.builtin

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, MediaTypes}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshal, Unmarshaller}
import akka.serialization.jackson.JacksonObjectMapperProvider
import akka.testkit.TestKit
import akka.util.ByteString
import com.fasterxml.jackson.databind.ObjectMapper
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

case class Name(first: String, last: String)

class JacksonMapperTest extends TestKit(ActorSystem()) with AnyFunSuiteLike with ScalaFutures with Matchers {
  import system.dispatcher

  class ScalaObjectMapper(mapper: ObjectMapper)
      extends ObjectMapper(mapper)
      with com.fasterxml.jackson.module.scala.ScalaObjectMapper

  private val scalaObjectMapper = new ScalaObjectMapper(
    JacksonObjectMapperProvider(system).getOrCreate("jackson-json", None)
  )

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A: Manifest]: FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(MediaTypes.`application/json`).mapWithCharset {
      case (ByteString.empty, _) => throw Unmarshaller.NoContentException
      case (data, charset)       => scalaObjectMapper.readValue[A](data.decodeString(charset.nioCharset.name))
    }

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[A]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`)(obj => scalaObjectMapper.writeValueAsString(obj))

  test("Jackson") {
    val vector = Vector(Name("Yang", "Jing"), Name("Yang", "Xunjing"))
    val entity = HttpEntity(ContentTypes.`application/json`, scalaObjectMapper.writeValueAsString(vector))
    entity.contentType shouldBe ContentTypes.`application/json`
    Unmarshal(entity).to[Vector[Name]].futureValue shouldBe vector
  }
}
