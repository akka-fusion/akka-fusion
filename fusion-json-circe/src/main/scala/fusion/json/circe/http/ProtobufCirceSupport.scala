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

package fusion.json.circe.http

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.unmarshalling.FromEntityUnmarshaller
import fusion.json.circe.CirceUtils
import io.circe.Json
import io.circe.Printer
import scalapb.GeneratedMessage
import scalapb.GeneratedMessageCompanion
import scalapb.Message

trait FailFastProtobufCirceSupport extends ProtobufCirceSupport with FailFastUnmarshaller

object FailFastProtobufCirceSupport extends FailFastProtobufCirceSupport

trait ProtobufCirceSupport extends BaseCirceSupport {

  implicit final def marshaller[A <: GeneratedMessage](
      implicit printer: Printer = Printer.noSpaces): ToEntityMarshaller[A] =
    jsonMarshaller(printer).compose(CirceUtils.toJson)

  implicit final def unmarshaller[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion]
      : FromEntityUnmarshaller[A] = {
    def decode(json: Json): A = CirceUtils.fromJson[A](json)
    jsonUnmarshaller.map(decode)
  }

}
