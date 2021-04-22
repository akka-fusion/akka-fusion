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

package fusion.inject.builtin

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.{ Marshaller, ToEntityMarshaller }
import akka.http.scaladsl.model.{ ContentTypes, MediaTypes }
import akka.http.scaladsl.unmarshalling.{ FromEntityUnmarshaller, Unmarshaller }
import akka.serialization.jackson.JacksonObjectMapperProvider
import akka.util.ByteString
import fusion.json.jackson.ScalaObjectMapper

class JacksonMapper {
  val system: ActorSystem = ActorSystem()
  val objectMapper = new ScalaObjectMapper(JacksonObjectMapperProvider(system).getOrCreate("jackson-json", None))

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A: Manifest]: FromEntityUnmarshaller[A] =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(MediaTypes.`application/json`).mapWithCharset {
      case (ByteString.empty, _) => throw Unmarshaller.NoContentException
      case (data, charset)       => objectMapper.readValue[A](data.decodeString(charset.nioCharset.name))
    }

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[A]: ToEntityMarshaller[A] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`)(obj => objectMapper.writeValueAsString(obj))
}
