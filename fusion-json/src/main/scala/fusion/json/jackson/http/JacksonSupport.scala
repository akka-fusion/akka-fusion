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
import java.lang.reflect.ParameterizedType
import java.lang.reflect.{Type => JType}

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import fusion.json.jackson.Jackson

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

/**
 * JSON marshalling/unmarshalling using an in-scope Jackson's ObjectMapper
 */
trait JacksonSupport {
  private def typeReference[T: TypeTag] = {
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

  /**
   * HTTP entity => `A`
   */
  implicit def unmarshaller[A](
      implicit
      ct: ClassTag[A],
      objectMapper: ObjectMapper = Jackson.defaultObjectMapper): FromEntityUnmarshaller[A] =
    Supports.jsonStringUnmarshaller.map { data =>
      objectMapper.readValue(data, ct.runtimeClass).asInstanceOf[A]
    }

  /**
   * `A` => HTTP entity
   */
  implicit def marshaller[A](implicit objectMapper: ObjectMapper = Jackson.defaultObjectMapper): ToEntityMarshaller[A] =
    JacksonHelper.marshaller[A](objectMapper)

}

object JacksonSupport extends JacksonSupport
