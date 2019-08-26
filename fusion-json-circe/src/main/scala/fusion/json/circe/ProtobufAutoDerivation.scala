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

package fusion.json.circe

import java.time.Instant

import com.google.protobuf.struct.Value.Kind
import com.google.protobuf.struct.ListValue
import com.google.protobuf.struct.NullValue
import com.google.protobuf.struct.Struct
import com.google.protobuf.struct.Value
import com.google.protobuf.timestamp.Timestamp
import io.circe._
import io.circe.generic.AutoDerivation
import io.circe.generic.auto._
import io.circe.syntax._

trait ProtobufAutoDerivation {
  implicit def protobufTimestampEncoder: Encoder[Timestamp] = (a: Timestamp) => {
    val inst = Instant.ofEpochSecond(a.seconds, a.nanos)
    Json.fromString(inst.toString)
  }
  implicit def protobufTimestampDecoder: Decoder[Timestamp] = (c: HCursor) => {
    c.top match {
      case Some(json) if json.isString =>
        c.as[String].map { text =>
          val inst = Instant.parse(text)
          Timestamp(inst.getEpochSecond, inst.getNano)
        }
      case Some(json) if json.isNumber =>
        c.as[Long].map { millis =>
          val inst = Instant.ofEpochMilli(millis)
          Timestamp(inst.getEpochSecond, inst.getNano)
        }
      case _ =>
        c.as[List[Long]].flatMap {
          case epochSeconds :: nano :: _ =>
            val inst = Instant.ofEpochSecond(epochSeconds, nano)
            Right(Timestamp(inst.getEpochSecond, inst.getNano))
          case other =>
            Left(DecodingFailure(s"无效的时间格式：$other，需要：[epochSeconds, nano]", Nil))
        }
    }
  }
  implicit def nullEncoder: Encoder[NullValue] = _ => Json.Null
  implicit def nullDecoder: Decoder[NullValue] = (c: HCursor) => {
    if (c.value.isNull) Right(NullValue.NULL_VALUE)
    else Left(DecodingFailure(s"${c.value} is not null", Nil))
  }

  implicit def kindEncoder: Encoder[Kind] = {
    case Kind.NullValue(_)   => Json.Null
    case Kind.NumberValue(v) => Json.fromDouble(v).get
    case Kind.StringValue(v) => Json.fromString(v)
    case Kind.BoolValue(v)   => Json.fromBoolean(v)
    case Kind.StructValue(v) => v.asJson
    case Kind.ListValue(v)   => Json.arr(v.values.map(value => value.asJson): _*)
    case Kind.Empty          => Json.Null
  }
  implicit def kindDecoder: Decoder[Kind] = (c: HCursor) => {
    try {
      val v = c.value
      val kind =
        if (v.isArray) {
          val arr = v.asArray.get.map(_.as[Value].right.get)
          Kind.ListValue(ListValue(arr))
        } else if (v.isBoolean) Kind.BoolValue(v.as[Boolean].right.get)
        else if (v.isNull) Kind.NullValue(NullValue.NULL_VALUE)
        else if (v.isNumber) Kind.NumberValue(v.as[Double].right.get)
        else if (v.isObject) {
          val fields = v.asObject.get.toMap.map {
            case (key, value) => key -> Value(value.as[Kind].getOrElse(Kind.NullValue(NullValue.NULL_VALUE)))
          }
          val struct = Struct(fields)
          Kind.StructValue(struct)
        } else if (v.isString) Kind.StringValue(v.as[String].right.get)
        else Kind.Empty
      Right(kind)
    } catch {
      case e: Throwable =>
        Left(DecodingFailure.fromThrowable(e, Nil))
    }
  }
  implicit def valueEncoder: Encoder[Value] = (a: Value) => a.kind.asJson
  implicit def valueDecoder: Decoder[Value] = (c: HCursor) => c.as[Kind].map(v => Value(v))
  implicit def listValueEncoder: Encoder[ListValue] = (a: ListValue) => Json.arr(a.values.map(_.asJson): _*)
  implicit def listValueDecoder: Decoder[ListValue] =
    (c: HCursor) =>
      try {
        val option = c.downArray.values.map(jsons => ListValue(jsons.map(_.as[Value].right.get).toSeq))
        Right(option.get)
      } catch {
        case e: Throwable =>
          Left(DecodingFailure.fromThrowable(e, Nil))
      }
  implicit def protobufStructEncoder: Encoder[Struct] = (a: Struct) => Json.obj(a.fields.mapValues(_.asJson).toSeq: _*)
  implicit def protobufStructDecoder: Decoder[Struct] =
    (c: HCursor) =>
      try {
        c.keys match {
          case Some(keys) =>
            Right(Struct(keys.map { key =>
              key -> c.get[Value](key).right.get
            }.toMap))
          case _ => Left(DecodingFailure("protobufStructDecoder", Nil))
        }
      } catch {
        case e: Throwable => Left(DecodingFailure.fromThrowable(e, Nil))
      }
  //  implicit def protobufAnyEncoder = new Encoder[com.google.protobuf.any.Any] {
  //    override def apply(a: com.google.protobuf.any.Any): Json = {
  //      ???
  //    }
  //  }
  //  implicit def protobufAnyDecoder = new Decoder[com.google.protobuf.any.Any] {
  //    override def apply(c: HCursor): Result[com.google.protobuf.any.Any] = {
  //      ???
  //    }
  //  }
}

object ProtobufAutoDerivation extends AutoDerivation with ProtobufAutoDerivation {}
