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
import com.fasterxml.jackson.databind.node._
import com.google.protobuf.struct
import com.google.protobuf.struct.Value.Kind
import fusion.json.JsonFormatException

import scala.jdk.CollectionConverters._

object StructFormat {

  def structValueWriter(v: struct.Value)(implicit mapper: ObjectMapper): JsonNode =
    v.kind match {
      case Kind.Empty              => NullNode.instance
      case Kind.NullValue(_)       => NullNode.instance
      case Kind.NumberValue(value) => new DoubleNode(value)
      case Kind.StringValue(value) => new TextNode(value)
      case Kind.BoolValue(value)   => BooleanNode.valueOf(value)
      case Kind.StructValue(value) => structWriter(value)
      case Kind.ListValue(value)   => listValueWriter(value)
    }

  def structValueParser(v: JsonNode): struct.Value = {
    val kind: struct.Value.Kind = v match {
      case _: NullNode        => Kind.NullValue(struct.NullValue.NULL_VALUE)
      case s: TextNode        => Kind.StringValue(value = s.asText())
      case num: IntNode       => Kind.NumberValue(value = num.doubleValue())
      case num: NumericNode   => Kind.NumberValue(value = num.doubleValue())
      case value: BooleanNode => Kind.BoolValue(value = value.booleanValue())
      case obj: ObjectNode    => Kind.StructValue(value = structParser(obj))
      case arr: ArrayNode     => Kind.ListValue(listValueParser(arr))
      case _                  => throw new RuntimeException("Unsupported")
    }
    struct.Value(kind = kind)
  }

  def structParser(v: JsonNode): struct.Struct =
    v match {
      case fields: ObjectNode =>
        struct.Struct(fields = fields.fieldNames().asScala.map(key => key -> structValueParser(fields.get(key))).toMap)
      case _ => throw new JsonFormatException("Expected an object")
    }

  def structWriter(v: struct.Struct)(implicit mapper: ObjectMapper): JsonNode = {
    val obj = mapper.createObjectNode()
    for ((key, value) <- v.fields) {
      obj.set(key, structValueWriter(value))
    }
    obj
  }

  def listValueParser(v: JsonNode): struct.ListValue =
    v match {
      case elems: ArrayNode =>
        com.google.protobuf.struct.ListValue(elems.asScala.map(structValueParser).toVector)
      case _ => throw new JsonFormatException("Expected a list")
    }

  def listValueWriter(v: struct.ListValue)(implicit mapper: ObjectMapper): ArrayNode = {
    val arr = mapper.createArrayNode()
    for (value <- v.values) {
      arr.add(structValueWriter(value))
    }
    arr
  }

  def nullValueParser(v: JsonNode): struct.NullValue =
    v match {
      case _: NullNode => com.google.protobuf.struct.NullValue.NULL_VALUE
      case _           => throw new JsonFormatException("Expected a null")
    }

  def nullValueWriter(v: struct.NullValue): JsonNode = NullNode.instance
}
