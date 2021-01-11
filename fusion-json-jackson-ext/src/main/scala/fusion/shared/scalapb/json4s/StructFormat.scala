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

package fusion.shared.scalapb.json4s

import com.google.protobuf.struct.Value.Kind
import org.json4s.JsonAST._
import com.google.protobuf.struct
import fusion.json.JsonFormatException

object StructFormat {

  def structValueWriter(v: struct.Value): JValue =
    v.kind match {
      case Kind.Empty              => JNull
      case Kind.NullValue(value)   => JNull
      case Kind.NumberValue(value) => JDouble(value)
      case Kind.StringValue(value) => JString(value)
      case Kind.BoolValue(value)   => JBool(value)
      case Kind.StructValue(value) => structWriter(value)
      case Kind.ListValue(value)   => listValueWriter(value)
    }

  def structValueParser(v: JValue): struct.Value = {
    val kind: struct.Value.Kind = v match {
      case JNothing      => Kind.NullValue(struct.NullValue.NULL_VALUE)
      case JNull         => Kind.NullValue(struct.NullValue.NULL_VALUE)
      case JString(s)    => Kind.StringValue(value = s)
      case JDouble(num)  => Kind.NumberValue(value = num)
      case JDecimal(num) => Kind.NumberValue(value = num.toDouble)
      case JLong(num)    => Kind.NumberValue(value = num.toDouble)
      case JInt(num)     => Kind.NumberValue(value = num.toDouble)
      case JBool(value)  => Kind.BoolValue(value = value)
      case obj: JObject  => Kind.StructValue(value = structParser(obj))
      case arr: JArray   => Kind.ListValue(listValueParser(arr))
      case JSet(set)     => throw new RuntimeException("Unsupported")
    }
    struct.Value(kind = kind)
  }

  def structParser(v: JValue): struct.Struct =
    v match {
      case JObject(fields) => struct.Struct(fields = fields.map(kv => (kv._1 -> structValueParser(kv._2))).toMap)
      case _               => throw new JsonFormatException("Expected an object")
    }

  def structWriter(v: struct.Struct): JValue =
    JObject(v.fields.mapValues(structValueWriter).toList)

  def listValueParser(v: JValue): struct.ListValue =
    v match {
      case JArray(elems) =>
        com.google.protobuf.struct.ListValue(elems.map(structValueParser))
      case _ => throw new JsonFormatException("Expected a list")
    }

  def listValueWriter(v: struct.ListValue): JArray =
    JArray(v.values.map(structValueWriter).toList)

  def nullValueParser(v: JValue): struct.NullValue =
    v match {
      case JNull =>
        com.google.protobuf.struct.NullValue.NULL_VALUE
      case _ => throw new JsonFormatException("Expected a null")
    }

  def nullValueWriter(v: struct.NullValue): JValue = JNull
}
