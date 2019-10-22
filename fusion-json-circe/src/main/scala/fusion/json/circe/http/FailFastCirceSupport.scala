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
import io.circe.Encoder
import io.circe.Printer

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
 * The unmarshaller fails fast, throwing the first `Error` encountered.
 *
 * To use automatic codec derivation, user needs to import `io.circe.generic.auto._`.
 */
object FailFastCirceSupport extends FailFastCirceSupport

/**
 * Automatic to and from JSON marshalling/unmarshalling using an in-scope circe protocol.
 * The unmarshaller fails fast, throwing the first `Error` encountered.
 *
 * To use automatic codec derivation import `io.circe.generic.auto._`.
 */
trait FailFastCirceSupport extends AnyCirceSupport with FailFastUnmarshaller

trait AnyCirceSupport extends BaseCirceSupport {

  /**
   * `A` => HTTP entity
   *
   * @tparam A type to encode
   * @return marshaller for any `A` value
   */
  implicit final def marshaller[A: Encoder](implicit printer: Printer = Printer.noSpaces): ToEntityMarshaller[A] =
    jsonMarshaller(printer).compose(Encoder[A].apply)

}
