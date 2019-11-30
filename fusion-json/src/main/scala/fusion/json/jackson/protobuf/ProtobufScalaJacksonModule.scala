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

package fusion.json.jackson.protobuf

import java.time.Instant
import java.util.Objects

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.module.scala.JacksonModule
import com.google.protobuf.struct.NullValue
import com.google.protobuf.struct.Value
import com.google.protobuf.struct.Value.Kind
import com.google.protobuf.timestamp.Timestamp

object Protobufs {
  val TIMESTAMP_CLS = classOf[Timestamp]
  val NULLVALUE_CLS = classOf[NullValue]
  val KIND_CLS = classOf[Kind]
  val VALUE_CLS = classOf[Value]

  val timestampSerializer = new JsonSerializer[Timestamp] {
    override def serialize(value: Timestamp, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      if (Objects.isNull(value)) {
        gen.writeNull()
      } else {
        gen.writeString(Instant.ofEpochSecond(value.seconds, value.nanos).toString)
      }
    }
  }

  val timestampDeserializer = new JsonDeserializer[Timestamp] {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Timestamp = {
      p.getValueAsString match {
        case null => null
        case text =>
          val inst = Instant.parse(text)
          Timestamp(inst.getEpochSecond, inst.getNano)
      }
    }
  }

  val nullValueSer = new JsonSerializer[NullValue] {
    override def serialize(value: NullValue, gen: JsonGenerator, serializers: SerializerProvider): Unit =
      gen.writeNull()
  }

  val nullValueDeser = new JsonDeserializer[NullValue] {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): NullValue = NullValue.NULL_VALUE
  }

  val kindSer = new JsonSerializer[Kind] {
    override def serialize(value: Kind, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      value match {
        case Kind.NumberValue(v) => gen.writeNumber(v)
        case Kind.StringValue(v) => gen.writeString(v)
        case Kind.BoolValue(v)   => gen.writeBoolean(v)
        case Kind.StructValue(v) => gen.writeObject(v)
        case Kind.ListValue(list) =>
          gen.writeStartArray()
          list.values.foreach(v => gen.writeObject(v.kind))
          gen.writeEndArray()
        case _ => gen.writeNull() // null, Kind.NullValue(_) Kind.Empty
      }
    }
  }

  val kindDeser = new JsonDeserializer[Kind] {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Kind = {
      val token = p.currentToken()
      println(token + " | " + p)
      token match {
        case JsonToken.VALUE_STRING                       => Kind.StringValue(p.getValueAsString)
        case JsonToken.VALUE_NUMBER_FLOAT                 => Kind.NumberValue(p.getValueAsDouble)
        case JsonToken.VALUE_NUMBER_INT                   => Kind.NumberValue(p.getValueAsInt)
        case JsonToken.VALUE_TRUE | JsonToken.VALUE_FALSE => Kind.BoolValue(p.getValueAsBoolean)
        case JsonToken.START_OBJECT                       =>
//          ctxt.readValue(p, KIND_CLS)
          null
        //          p.readValueAs(classOf[Kind])
        //        case JsonToken.START_ARRAY                        => p.readValueAs(classOf[Kind])
        case _ => Kind.NullValue(NullValue.NULL_VALUE)
      }
    }
  }

  val valueSer = new JsonSerializer[Value] {
    override def serialize(value: Value, gen: JsonGenerator, serializers: SerializerProvider): Unit =
      gen.writeObject(value.kind)
  }

  val valueDeser = new JsonDeserializer[Value] {
    override def deserialize(p: JsonParser, ctxt: DeserializationContext): Value = {
      val token = p.currentToken()
      token match {
        case JsonToken.START_OBJECT => p.getObjectId
      }
      Value(p.readValueAs(classOf[Value.Kind]))
    }
  }
}

class ProtobufScalaJacksonModule extends JacksonModule {
  import Protobufs._

  object ProtobufScalaSers extends Serializers.Base {
    override def findSerializer(
        config: SerializationConfig,
        `type`: JavaType,
        beanDesc: BeanDescription): JsonSerializer[_] = {
      val rawCls = `type`.getRawClass
      if (TIMESTAMP_CLS.isAssignableFrom(rawCls)) {
        timestampSerializer
      } else if (NULLVALUE_CLS.isAssignableFrom(rawCls)) {
        nullValueSer
      } else if (VALUE_CLS.isAssignableFrom(rawCls)) {
        valueSer
      } else if (KIND_CLS.isAssignableFrom(rawCls)) {
        kindSer
      } else {
        super.findSerializer(config, `type`, beanDesc)
      }
    }
  }

  object ProtobufScalaDesers extends Deserializers.Base {
    override def findBeanDeserializer(
        `type`: JavaType,
        config: DeserializationConfig,
        beanDesc: BeanDescription): JsonDeserializer[_] = {
      val rawCls = `type`.getRawClass
      if (TIMESTAMP_CLS.isAssignableFrom(rawCls)) {
        timestampDeserializer
      } else if (NULLVALUE_CLS.isAssignableFrom(rawCls)) {
        nullValueDeser
      } else if (VALUE_CLS.isAssignableFrom(rawCls)) {
        valueDeser
      } else if (KIND_CLS.isAssignableFrom(rawCls)) {
        kindDeser
      } else {
        super.findBeanDeserializer(`type`, config, beanDesc)
      }
    }
  }

  override def getModuleName(): String = getClass.getSimpleName

  this += ProtobufScalaSers
  this += ProtobufScalaDesers
}
