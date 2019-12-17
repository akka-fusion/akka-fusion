/*
 * Copyright 2019 akka-fusion.com
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

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node._
import com.google.protobuf.ByteString
import com.google.protobuf.descriptor.FieldDescriptorProto
import com.google.protobuf.descriptor.FieldDescriptorProto.Type
import com.google.protobuf.duration.Duration
import com.google.protobuf.field_mask.FieldMask
import com.google.protobuf.struct.NullValue
import com.google.protobuf.timestamp.Timestamp
import fusion.json.Durations
import fusion.json.JsonFormatException
import fusion.json.Timestamps
import fusion.json.jackson.Jackson
import fusion.json.jackson.protobuf.JacksonFormat.GenericCompanion
import helloscala.common.util.StringUtils
import scalapb._
import scalapb.descriptors._
import Jackson.defaultObjectMapper
import scala.jdk.CollectionConverters._
import scala.reflect.ClassTag

case class Formatter[T](writer: (Printer, T) => JsonNode, parser: (Parser, JsonNode) => T)

case class FormatRegistry(
    messageFormatters: Map[Class[_], Formatter[_]] = Map.empty,
    enumFormatters: Map[EnumDescriptor, Formatter[EnumValueDescriptor]] = Map.empty /*,
    registeredCompanions: Seq[GenericCompanion] = Seq.empty*/ ) {
  def registerMessageFormatter[T <: GeneratedMessage](
      writer: (Printer, T) => JsonNode,
      parser: (Parser, JsonNode) => T)(implicit ct: ClassTag[T]): FormatRegistry = {
    copy(messageFormatters = messageFormatters + (ct.runtimeClass -> Formatter(writer, parser)))
  }

  def registerEnumFormatter[E <: GeneratedEnum](
      writer: (Printer, EnumValueDescriptor) => JsonNode,
      parser: (Parser, JsonNode) => EnumValueDescriptor)(implicit cmp: GeneratedEnumCompanion[E]): FormatRegistry = {
    copy(enumFormatters = enumFormatters + (cmp.scalaDescriptor -> Formatter(writer, parser)))
  }

  def registerWriter[T <: GeneratedMessage: ClassTag](writer: T => JsonNode, parser: JsonNode => T): FormatRegistry = {
    registerMessageFormatter((p: Printer, t: T) => writer(t), ((p: Parser, v: JsonNode) => parser(v)))
  }

  def getMessageWriter[T](klass: Class[_ <: T]): Option[(Printer, T) => JsonNode] = {
    messageFormatters.get(klass).asInstanceOf[Option[Formatter[T]]].map(_.writer)
  }

  def getMessageParser[T](klass: Class[_ <: T]): Option[(Parser, JsonNode) => T] = {
    messageFormatters.get(klass).asInstanceOf[Option[Formatter[T]]].map(_.parser)
  }

  def getEnumWriter(descriptor: EnumDescriptor): Option[(Printer, EnumValueDescriptor) => JsonNode] = {
    enumFormatters.get(descriptor).map(_.writer)
  }

  def getEnumParser(descriptor: EnumDescriptor): Option[(Parser, JsonNode) => EnumValueDescriptor] = {
    enumFormatters.get(descriptor).map(_.parser)
  }
}

object Parser {
  final private case class ParserConfig(
      isIgnoringUnknownFields: Boolean,
      formatRegistry: FormatRegistry,
      typeRegistry: TypeRegistry)
}

class Parser private (config: Parser.ParserConfig)(implicit mapper: ObjectMapper) {
  def this()(implicit mapper: ObjectMapper) =
    this(Parser.ParserConfig(isIgnoringUnknownFields = false, JacksonFormat.defaultRegistry, TypeRegistry.empty))

  def ignoringUnknownFields: Parser = new Parser(config.copy(isIgnoringUnknownFields = true))

  def withFormatRegistry(formatRegistry: FormatRegistry) =
    new Parser(config.copy(formatRegistry = formatRegistry))

  def withTypeRegistry(typeRegistry: TypeRegistry) =
    new Parser(config.copy(typeRegistry = typeRegistry))

  def typeRegistry: TypeRegistry = config.typeRegistry

  def fromJsonString[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](str: String): A = {
    fromJson(mapper.readTree(str))
  }

  def fromJson[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](value: JsonNode): A = {
    fromJson(value, false)
  }

  def fromJson[A <: GeneratedMessage with Message[A]](value: JsonNode, skipTypeUrl: Boolean)(
      implicit cmp: GeneratedMessageCompanion[A]): A = {
    cmp.messageReads.read(fromJsonToPMessage(cmp, value, skipTypeUrl))
  }

  private def fromJsonToPMessage(cmp: GeneratedMessageCompanion[_], value: JsonNode, skipTypeUrl: Boolean): PMessage = {
    def parseValue(fd: FieldDescriptor, value: JsonNode): PValue = {
      if (fd.isMapField) {
        value match {
          case vals: ObjectNode =>
            val mapEntryDesc = fd.scalaType.asInstanceOf[ScalaType.Message].descriptor
            val keyDescriptor = mapEntryDesc.findFieldByNumber(1).get
            val valueDescriptor = mapEntryDesc.findFieldByNumber(2).get
            PRepeated(
              vals
                .fieldNames()
                .asScala
                .map { key =>
                  val keyObj = keyDescriptor.scalaType match {
                    case ScalaType.Boolean => PBoolean(java.lang.Boolean.valueOf(key))
                    case ScalaType.Double  => PDouble(java.lang.Double.valueOf(key))
                    case ScalaType.Float   => PFloat(java.lang.Float.valueOf(key))
                    case ScalaType.Int     => PInt(java.lang.Integer.valueOf(key))
                    case ScalaType.Long    => PLong(java.lang.Long.valueOf(key))
                    case ScalaType.String  => PString(key)
                    case _                 => throw new RuntimeException(s"Unsupported type for key for ${fd.name}")
                  }
                  val jValue = vals.get(key)
                  PMessage(
                    Map(
                      keyDescriptor -> keyObj,
                      valueDescriptor -> parseSingleValue(
                        cmp.messageCompanionForFieldNumber(fd.number),
                        valueDescriptor,
                        jValue)))
                }
                .toVector)
          case _ =>
            throw new JsonFormatException(
              s"Expected an object for map field ${fd.name} of ${fd.containingMessage.name}")
        }
      } else if (fd.isRepeated) {
        value match {
          case vals: ArrayNode => PRepeated(vals.asScala.map(parseSingleValue(cmp, fd, _)).toVector)
          case _ =>
            throw new JsonFormatException(
              s"Expected an array for repeated field ${fd.name} of ${fd.containingMessage.name}")
        }
      } else parseSingleValue(cmp, fd, value)
    }

    config.formatRegistry.getMessageParser(cmp.defaultInstance.getClass) match {
      case Some(p) => p(this, value).asInstanceOf[GeneratedMessage].toPMessage
      case None =>
        value match {
          case fields: ObjectNode =>
            val fieldMap = JacksonFormat.MemorizedFieldNameMap(cmp.scalaDescriptor)
            val valueMapBuilder = Map.newBuilder[FieldDescriptor, PValue]
            fields.fieldNames().asScala.foreach { name =>
              val jsonNode = fields.get(name)
              if (fieldMap.contains(name)) {
                if (!jsonNode.isNull) {
                  val fd = fieldMap(name)
                  valueMapBuilder += (fd -> parseValue(fd, jsonNode))
                }
              } else if (!config.isIgnoringUnknownFields && !(skipTypeUrl && name == "@type")) {
                throw new JsonFormatException(s"Cannot find field: ${name} in message ${cmp.scalaDescriptor.fullName}")
              }
            }

            PMessage(valueMapBuilder.result())
          case _ =>
            throw new JsonFormatException(s"Expected an object, found ${value}")
        }
    }
  }

  def defaultEnumParser(enumDescriptor: EnumDescriptor, value: JsonNode): EnumValueDescriptor = value match {
    case v: IntNode =>
      enumDescriptor
        .findValueByNumber(v.asInt(0))
        .getOrElse(
          throw new JsonFormatException(s"Invalid enum value: ${v.asInt(0)} for enum type: ${enumDescriptor.fullName}"))
    case s: TextNode =>
      enumDescriptor.values
        .find(_.name == s.asText(""))
        .getOrElse(throw new JsonFormatException(s"Unrecognized enum value '$s'"))
    case _ =>
      throw new JsonFormatException(s"Unexpected value ($value) for enum ${enumDescriptor.fullName}")
  }

  protected def parseSingleValue(
      containerCompanion: GeneratedMessageCompanion[_],
      fd: FieldDescriptor,
      value: JsonNode): PValue = fd.scalaType match {
    case ScalaType.Enum(ed) =>
      PEnum(config.formatRegistry.getEnumParser(ed) match {
        case Some(parser) => parser(this, value)
        case None         => defaultEnumParser(ed, value)
      })
    case ScalaType.Message(md) =>
      fromJsonToPMessage(containerCompanion.messageCompanionForFieldNumber(fd.number), value, false)
    case st =>
      JacksonFormat.parsePrimitive(
        fd.protoType,
        value,
        throw new JsonFormatException(
          s"Unexpected value ($value) for field ${fd.name} of ${fd.containingMessage.name}"))
  }
}

object JacksonFormat {
  import com.google.protobuf.wrappers

  type GenericCompanion = GeneratedMessageCompanion[T] forSome { type T <: GeneratedMessage with Message[T] }

  def defaultRegistry(implicit mapper: ObjectMapper) = {
    FormatRegistry()
      .registerWriter(
        (d: Duration) => new TextNode(Durations.writeDuration(d)),
        jv =>
          jv match {
            case str: TextNode => Durations.parseDuration(str.asText(""))
            case _             => throw new JsonFormatException("Expected a string.")
          })
      .registerWriter[Timestamp]((t: Timestamp) => new TextNode(Timestamps.writeTimestamp(t)), wrapperParserTimestamp)
      .registerWriter[FieldMask]((m: FieldMask) => new TextNode(FieldMaskUtil.toJsonString(m)), wrapperParserFieldMask)
      .registerMessageFormatter[wrappers.DoubleValue](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.DoubleValue])
      .registerMessageFormatter[wrappers.FloatValue](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.FloatValue])
      .registerMessageFormatter[wrappers.Int32Value](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.Int32Value])
      .registerMessageFormatter[wrappers.Int64Value](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.Int64Value])
      .registerMessageFormatter[wrappers.UInt32Value](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.UInt32Value])
      .registerMessageFormatter[wrappers.UInt64Value](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.UInt64Value])
      .registerMessageFormatter[wrappers.BoolValue](primitiveWrapperWriter, primitiveWrapperParser[wrappers.BoolValue])
      .registerMessageFormatter[wrappers.BytesValue](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.BytesValue])
      .registerMessageFormatter[wrappers.StringValue](
        primitiveWrapperWriter,
        primitiveWrapperParser[wrappers.StringValue])
      .registerEnumFormatter[NullValue](
        (_, _) => NullNode.instance,
        (parser, value) =>
          value match {
            case _: NullNode => NullValue.NULL_VALUE.scalaValueDescriptor
            case _           => parser.defaultEnumParser(NullValue.scalaDescriptor, value)
          })
      .registerWriter[com.google.protobuf.struct.Value](StructFormat.structValueWriter, StructFormat.structValueParser)
      .registerWriter[com.google.protobuf.struct.Struct](StructFormat.structWriter, StructFormat.structParser)
      .registerWriter[com.google.protobuf.struct.ListValue](StructFormat.listValueWriter, StructFormat.listValueParser)
      .registerMessageFormatter[com.google.protobuf.any.Any](AnyFormat.anyWriter, AnyFormat.anyParser)
  }

  private def wrapperParserFieldMask(jv: JsonNode): FieldMask = jv match {
    case str: TextNode => FieldMaskUtil.fromJsonString(str.textValue())
    case _             => throw new JsonFormatException("Expected a string.")
  }

  private def wrapperParserTimestamp(jv: JsonNode): Timestamp = jv match {
    case str: TextNode => Timestamps.parseTimestamp(str.textValue())
    case _             => throw new JsonFormatException("Expected a string.")
  }

  def primitiveWrapperWriter[T <: GeneratedMessage with Message[T]](
      implicit cmp: GeneratedMessageCompanion[T]): (Printer, T) => JsonNode = {
    val fieldDesc = cmp.scalaDescriptor.findFieldByNumber(1).get
    (printer, t) => printer.serializeSingleValue(fieldDesc, t.getField(fieldDesc), formattingLongAsNumber = false)
  }

  def primitiveWrapperParser[T <: GeneratedMessage with Message[T]](
      implicit cmp: GeneratedMessageCompanion[T]): (Parser, JsonNode) => T = {
    val fieldDesc = cmp.scalaDescriptor.findFieldByNumber(1).get
    (parser, jv) =>
      cmp.messageReads.read(
        PMessage(
          Map(
            fieldDesc -> JacksonFormat.parsePrimitive(
              fieldDesc.protoType,
              jv,
              throw new JsonFormatException(s"Unexpected value for ${cmp.scalaDescriptor.name}")))))
  }

  val printer = new Printer()
  val parser = new Parser()

  def toJsonString[A <: GeneratedMessage](m: A): String = printer.print(m)

//  def toJson[A <: GeneratedMessage](m: A): JsonNode = printer.toJson(m)

  def fromJson[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](value: JsonNode): A = {
    parser.fromJson(value)
  }

  def fromJsonString[A <: GeneratedMessage with Message[A]: GeneratedMessageCompanion](str: String): A = {
    parser.fromJsonString(str)
  }

//  implicit def protoToReader[T <: GeneratedMessage with Message[T]: GeneratedMessageCompanion]: Reader[T] =
//    new Reader[T] {
//      def read(value: JsonNode): T = parser.fromJson(value)
//    }

//  implicit def protoToWriter[T <: GeneratedMessage with Message[T]]: Writer[T] = new Writer[T] {
//    def write(obj: T): JsonNode = printer.toJson(obj)
//  }

  def defaultValue(fd: FieldDescriptor): PValue = {
    require(fd.isOptional)
    fd.scalaType match {
      case ScalaType.Int        => PInt(0)
      case ScalaType.Long       => PLong(0L)
      case ScalaType.Float      => PFloat(0)
      case ScalaType.Double     => PDouble(0)
      case ScalaType.Boolean    => PBoolean(false)
      case ScalaType.String     => PString("")
      case ScalaType.ByteString => PByteString(ByteString.EMPTY)
      case ScalaType.Enum(ed)   => PEnum(ed.values(0))
      case ScalaType.Message(_) => throw new RuntimeException("No default value for message")
    }
  }

  def parsePrimitive(protoType: FieldDescriptorProto.Type, value: JsonNode, onError: => PValue): PValue =
    (protoType, value) match {
      case (Type.TYPE_UINT32 | Type.TYPE_FIXED32, x: IntNode)                    => parseUint32(x.intValue().toString)
      case (Type.TYPE_UINT32 | Type.TYPE_FIXED32, x: DoubleNode)                 => parseUint32(x.doubleValue().toString)
      case (Type.TYPE_UINT32 | Type.TYPE_FIXED32, x: DecimalNode)                => parseUint32(x.decimalValue().toString)
      case (Type.TYPE_UINT32 | Type.TYPE_FIXED32, x: TextNode)                   => parseUint32(x.textValue())
      case (Type.TYPE_SINT32 | Type.TYPE_INT32 | Type.TYPE_SFIXED32, x: IntNode) => parseInt32(x.intValue().toString)
      case (Type.TYPE_SINT32 | Type.TYPE_INT32 | Type.TYPE_SFIXED32, x: DoubleNode) =>
        parseInt32(x.doubleValue().toString)
      case (Type.TYPE_SINT32 | Type.TYPE_INT32 | Type.TYPE_SFIXED32, x: DecimalNode) =>
        parseInt32(x.decimalValue().toString)
      case (Type.TYPE_SINT32 | Type.TYPE_INT32 | Type.TYPE_SFIXED32, x: TextNode) => parseInt32(x.textValue())

      case (Type.TYPE_UINT64 | Type.TYPE_FIXED64, x: IntNode)                    => parseUint64(x.intValue().toString)
      case (Type.TYPE_UINT64 | Type.TYPE_FIXED64, x: DoubleNode)                 => parseUint64(x.doubleValue().toString)
      case (Type.TYPE_UINT64 | Type.TYPE_FIXED64, x: DecimalNode)                => parseUint64(x.decimalValue().toString)
      case (Type.TYPE_UINT64 | Type.TYPE_FIXED64, x: TextNode)                   => parseUint64(x.textValue())
      case (Type.TYPE_SINT64 | Type.TYPE_INT64 | Type.TYPE_SFIXED64, x: IntNode) => parseInt64(x.intValue().toString)
      case (Type.TYPE_SINT64 | Type.TYPE_INT64 | Type.TYPE_SFIXED64, x: DoubleNode) =>
        parseInt64(x.doubleValue().toString)
      case (Type.TYPE_SINT64 | Type.TYPE_INT64 | Type.TYPE_SFIXED64, x: DecimalNode) =>
        parseInt64(x.decimalValue().toString)
      case (Type.TYPE_SINT64 | Type.TYPE_INT64 | Type.TYPE_SFIXED64, x: TextNode) => parseInt64(x.textValue())

      case (Type.TYPE_DOUBLE, x: DoubleNode)                                        => parseDouble(x.doubleValue().toString)
      case (Type.TYPE_DOUBLE, x: IntNode)                                           => parseDouble(x.intValue().toString)
      case (Type.TYPE_DOUBLE, x: DecimalNode)                                       => parseDouble(x.decimalValue().toString)
      case (Type.TYPE_DOUBLE, v: TextNode)                                          => parseDouble(v.textValue())
      case (Type.TYPE_FLOAT, x: DoubleNode)                                         => parseFloat(x.doubleValue().toString)
      case (Type.TYPE_FLOAT, x: IntNode)                                            => parseFloat(x.intValue().toString)
      case (Type.TYPE_FLOAT, x: DecimalNode)                                        => parseFloat(x.decimalValue().toString)
      case (Type.TYPE_FLOAT, v: TextNode)                                           => parseFloat(v.textValue())
      case (Type.TYPE_BOOL, b: BooleanNode)                                         => PBoolean(b.booleanValue())
      case (Type.TYPE_BOOL, v: TextNode) if v.textValue().equalsIgnoreCase("true")  => PBoolean(true)
      case (Type.TYPE_BOOL, v: TextNode) if v.textValue().equalsIgnoreCase("false") => PBoolean(false)
      case (Type.TYPE_STRING, s: TextNode)                                          => PString(s.textValue())
      case (Type.TYPE_BYTES, s: TextNode) =>
        PByteString(ByteString.copyFrom(Base64Variants.getDefaultVariant.decode(s.textValue())))
      case _ => onError
    }

  def parseBigDecimal(value: String): BigDecimal = {
    try {
      // JSON doesn't distinguish between integer values and floating point values so "1" and
      // "1.000" are treated as equal in JSON. For this reason we accept floating point values for
      // integer fields as well as long as it actually is an integer (i.e., round(value) == value).
      BigDecimal(value)
    } catch {
      case e: Exception =>
        throw JsonFormatException(s"Not a numeric value: $value", e)
    }
  }

  def parseInt32(value: String): PValue = {
    try {
      PInt(value.toInt)
    } catch {
      case _: Exception =>
        try {
          PInt(parseBigDecimal(value).toIntExact)
        } catch {
          case e: Exception =>
            throw JsonFormatException(s"Not an int32 value: $value", e)
        }
    }
  }

  def parseInt64(value: String): PValue = {
    try {
      PLong(value.toLong)
    } catch {
      case _: Exception =>
        val bd = parseBigDecimal(value)
        try {
          PLong(bd.toLongExact)
        } catch {
          case e: Exception =>
            throw JsonFormatException(s"Not an int64 value: $value", e)
        }
    }
  }

  def parseUint32(value: String): PValue = {
    try {
      val result = value.toLong
      if (result < 0 || result > 0xFFFFFFFFL) throw new JsonFormatException(s"Out of range uint32 value: $value")
      return PInt(result.toInt)
    } catch {
      case e: JsonFormatException => throw e
      case e: Exception           => // Fall through.
    }
    parseBigDecimal(value).toBigIntExact
      .map { intVal =>
        if (intVal < 0 || intVal > 0xFFFFFFFFL) throw new JsonFormatException(s"Out of range uint32 value: $value")
        PLong(intVal.intValue)
      }
      .getOrElse {
        throw new JsonFormatException(s"Not an uint32 value: $value")
      }
  }

  val MAX_UINT64 = BigInt("FFFFFFFFFFFFFFFF", 16)

  def parseUint64(value: String): PValue = {
    parseBigDecimal(value).toBigIntExact
      .map { intVal =>
        if (intVal < 0 || intVal > MAX_UINT64) {
          throw new JsonFormatException(s"Out of range uint64 value: $value")
        }
        PLong(intVal.longValue)
      }
      .getOrElse {
        throw new JsonFormatException(s"Not an uint64 value: $value")
      }
  }

  def parseDouble(value: String): PDouble = value match {
    case "NaN"       => PDouble(Double.NaN)
    case "Infinity"  => PDouble(Double.PositiveInfinity)
    case "-Infinity" => PDouble(Double.NegativeInfinity)
    case v =>
      try {
        val bd = new java.math.BigDecimal(v)
        if (bd.compareTo(MAX_DOUBLE) > 0 || bd.compareTo(MIN_DOUBLE) < 0) {
          throw new JsonFormatException("Out of range double value: " + v)
        }
        PDouble(bd.doubleValue)
      } catch {
        case e: JsonFormatException => throw e
        case e: Exception =>
          throw new JsonFormatException("Not a double value: " + v)
      }
  }

  def parseFloat(str: String): PFloat = str match {
    case "NaN"       => PFloat(Float.NaN)
    case "Infinity"  => PFloat(Float.PositiveInfinity)
    case "-Infinity" => PFloat(Float.NegativeInfinity)
    case v =>
      try {
        val value = java.lang.Double.parseDouble(v)
        if ((value > Float.MaxValue * (1.0 + EPSILON)) ||
            (value < -Float.MaxValue * (1.0 + EPSILON))) {
          throw new JsonFormatException("Out of range float value: " + value)
        }
        PFloat(value.toFloat)
      } catch {
        case e: JsonFormatException => throw e
        case e: Exception =>
          throw new JsonFormatException("Not a float value: " + v)
      }
  }

  def jsonName(fd: FieldDescriptor): String = {
    // protoc<3 doesn't know about json_name, so we fill it in if it's not populated.
    fd.asProto.jsonName.getOrElse(StringUtils.snakeCaseToCamelCase(fd.asProto.getName))
  }

  /** Given a message descriptor, provides a map from field names to field descriptors. */
  object MemorizedFieldNameMap {
    // The cached map. For thread-safety, we keep a changing references to an immutable map.
    private var fieldNameMap: Map[Descriptor, Map[String, FieldDescriptor]] = Map.empty

    def apply(descriptor: Descriptor): Map[String, FieldDescriptor] = {
      if (fieldNameMap.contains(descriptor)) fieldNameMap(descriptor)
      else {
        val mapBuilder = Map.newBuilder[String, FieldDescriptor]
        descriptor.fields.foreach { fd =>
          mapBuilder += fd.name -> fd
          mapBuilder += JacksonFormat.jsonName(fd) -> fd
        }
        val result = mapBuilder.result()
        fieldNameMap = fieldNameMap + (descriptor -> result)
        result
      }
    }
  }

  // From protobuf-java's JacksonFormat.java:
  val EPSILON: Double = 1e-6

  val MORE_THAN_ONE = new java.math.BigDecimal(String.valueOf(1.toDouble + EPSILON))

  val MAX_DOUBLE = new java.math.BigDecimal(String.valueOf(Double.MaxValue)).multiply(MORE_THAN_ONE)

  val MIN_DOUBLE = new java.math.BigDecimal(String.valueOf(Double.MinValue)).multiply(MORE_THAN_ONE)
}
