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

package fusion.jackson.protobuf

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.google.protobuf.any.{ Any => PBAny }
import fusion.json.JsonFormatException

object AnyFormat {
  def anyWriter(implicit mapper: ObjectMapper): (Printer, PBAny) => JsonNode = {
    case (printer, any) =>
      // Find the companion so it can be used to JSON-serialize the message. Perhaps this can be circumvented by
      // including the original GeneratedMessage with the Any (at least in memory).
      val cmp = printer.typeRegistry
        .findType(any.typeUrl)
        .getOrElse(throw new IllegalStateException(
          s"Unknown type ${any.typeUrl} in Any.  Add a TypeRegistry that supports this type to the Printer."))

      // Unpack the message...
      val message = any.unpack(cmp)

      // ... and add the @type marker to the resulting JSON
      printer.toJson(message) match {
        case fields: ObjectNode =>
          fields.put("@type", any.typeUrl) // JObject(("@type" -> JString(any.typeUrl)) +: fields)
        case value =>
          // Safety net, this shouldn't happen
          throw new IllegalStateException(s"Message of type ${any.typeUrl} emitted non-object JSON: $value")
      }
  }

  val anyParser: (Parser, JsonNode) => PBAny = {
    case (parser, obj @ (fields: ObjectNode)) =>
      obj.get("@type") match {
        case textNode: TextNode =>
          val typeUrl = textNode.asText()
          val cmp = parser.typeRegistry
            .findType(typeUrl)
            .getOrElse(throw new JsonFormatException(
              s"Unknown type $typeUrl in Any.  Add a TypeRegistry that supports this type to the Parser."))
          val message = parser.fromJson(obj, true)(cmp)
          PBAny(typeUrl = typeUrl, value = message.toByteString)

        case _: MissingNode =>
          throw new JsonFormatException(s"Missing type url when parsing $obj")

        case unknown =>
          throw new JsonFormatException(s"Expected string @type field, got $unknown")
      }

    case (_, unknown) =>
      throw new JsonFormatException(s"Expected an object, got $unknown")
  }
}
