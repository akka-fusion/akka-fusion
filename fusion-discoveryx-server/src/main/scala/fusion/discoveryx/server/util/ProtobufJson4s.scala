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

import org.json4s.JValue
import org.json4s.jackson.JsonMethods
import scalapb.json4s.{ Parser, Printer }
import scalapb.{ GeneratedMessage, GeneratedMessageCompanion, Message }

object ProtobufJson4s {
  val printer: Printer = new Printer().includingDefaultValueFields.formattingEnumsAsNumber.formattingLongAsNumber
  val parser: Parser = new Parser().ignoringUnknownFields

  def toJsonString[A <: GeneratedMessage](m: A): String = JsonMethods.compact(toJson(m))

  def toJsonPrettyString[A <: GeneratedMessage](m: A): String = JsonMethods.pretty(toJson(m))

  def toJson[A <: GeneratedMessage](m: A): JValue = printer.toJson(m)

  @inline def parse[A <: GeneratedMessage](m: A): JValue = toJson(m)

  def fromJson[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](value: JValue): A =
    parser.fromJson(value)

  def fromJsonString[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](str: String): A =
    parser.fromJsonString(str)

  @inline def extract[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](value: JValue): A =
    fromJson(value)
}
