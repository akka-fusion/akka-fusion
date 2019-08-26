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

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.util.ByteString
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.json.jackson.Jackson

import scala.reflect.ClassTag

/**
 * JSON marshalling/unmarshalling using an in-scope Jackson's ObjectMapper
 */
trait JacksonSupport {

  private val jsonStringUnmarshaller =
    Unmarshaller.byteStringUnmarshaller.forContentTypes(MediaTypes.`application/json`).mapWithCharset {
      case (ByteString.empty, _) => throw Unmarshaller.NoContentException
      case (data, charset)       => data.decodeString(charset.nioCharset.name)
    }

  //  private val jsonStringMarshaller = Marshaller.stringMarshaller(MediaTypes.`application/json`)

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A](
      implicit
      ct: ClassTag[A],
      objectMapper: ObjectMapper = Jackson.defaultObjectMapper): FromEntityUnmarshaller[A] =
    jsonStringUnmarshaller.map { data =>
      objectMapper.readValue(data, ct.runtimeClass).asInstanceOf[A]
    }

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[A](implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper): ToEntityMarshaller[A] =
    //    jsonStringMarshaller.compose(objectMapper.writeValueAsString)
    JacksonHelper.marshaller[A](objectMapper)

}

object JacksonSupport extends JacksonSupport
