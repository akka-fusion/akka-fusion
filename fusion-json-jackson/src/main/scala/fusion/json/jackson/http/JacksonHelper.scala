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

///*
// * Copyright 2019 helloscala.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fusion.json.jackson.http
//
//import java.io.IOException
//
//import akka.Done
//import akka.http.javadsl.marshalling.Marshaller
//import akka.http.javadsl.model.HttpEntity
//import akka.http.javadsl.model.MediaTypes
//import akka.http.javadsl.model.RequestEntity
//import akka.http.javadsl.unmarshalling.Unmarshaller
//import akka.util.ByteString
//import com.fasterxml.jackson.core.JsonProcessingException
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.fasterxml.jackson.module.scala.ScalaObjectMapper
//import fusion.json.jackson.Jackson
//
//object JacksonHelper {
//  val empty = new java.util.HashMap[String, String]
//
//  private def scalaObjectMapper(objectMapper: ObjectMapper): ObjectMapper with ScalaObjectMapper = objectMapper match {
//    case value: ObjectMapper with ScalaObjectMapper => value
//    case _                                          => new fusion.json.jackson.ScalaObjectMapper(objectMapper)
//  }
//
//  def marshaller[T]: Marshaller[T, RequestEntity] = marshaller(Jackson.defaultObjectMapper)
//
//  def marshaller[T](mapper: ObjectMapper): Marshaller[T, RequestEntity] =
//    Marshaller.wrapEntity((u: T) => toJSON(mapper, u), Marshaller.stringToEntity, MediaTypes.APPLICATION_JSON)
//
//  def byteStringUnmarshaller[T](expectedType: Class[T]): Unmarshaller[ByteString, T] =
//    byteStringUnmarshaller(Jackson.defaultObjectMapper, expectedType)
//
//  def unmarshaller[T](expectedType: Class[T]): Unmarshaller[HttpEntity, T] =
//    unmarshaller(Jackson.defaultObjectMapper, expectedType)
//
//  def unmarshaller[T](mapper: ObjectMapper, expectedType: Class[T]): Unmarshaller[HttpEntity, T] =
//    Unmarshaller
//      .forMediaType(MediaTypes.APPLICATION_JSON, Unmarshaller.entityToString)
//      .thenApply((s: String) => fromJSON(mapper, s, expectedType))
//
//  def byteStringUnmarshaller[T](mapper: ObjectMapper, expectedType: Class[T]): Unmarshaller[ByteString, T] =
//    Unmarshaller.sync((s: ByteString) => fromJSON(mapper, s.utf8String, expectedType))
//
//  private def toJSON(mapper: ObjectMapper, obj: Any): String =
//    try {
//      if (obj.isInstanceOf[Done]) return mapper.writeValueAsString(empty)
//      mapper.writeValueAsString(obj)
//    } catch {
//      case e: JsonProcessingException =>
//        throw new IllegalArgumentException("Cannot marshal to JSON: " + obj, e)
//    }
//
//  private def fromJSON[T](mapper: ObjectMapper, json: String, expectedType: Class[T]): T =
//    try mapper.readerFor(expectedType).readValue(json)
//    catch {
//      case e: IOException =>
//        throw new IllegalArgumentException("Cannot unmarshal JSON as " + expectedType.getSimpleName, e)
//    }
//}
