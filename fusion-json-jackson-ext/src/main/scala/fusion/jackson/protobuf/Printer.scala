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

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.databind.node._
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.google.protobuf.descriptor.FieldDescriptorProto
import fusion.jackson.protobuf.JacksonFormat.GenericCompanion
import fusion.json.JsonFormatException
import scalapb.descriptors._
import scalapb.{ GeneratedFileObject, GeneratedMessage, GeneratedMessageCompanion, Message }

/**
 * TypeRegistry is used to map the @type field in Any messages to a ScalaPB generated message.
 *
 * TypeRegistries are added to Printers and Parsers to enable printing and parsing of Any messages.
 */
case class TypeRegistry(
    companions: Map[String, GenericCompanion] = Map.empty,
    private val filesSeen: Set[String] = Set.empty) {
  def addMessage[T <: GeneratedMessage with Message[T]](implicit cmp: GeneratedMessageCompanion[T]): TypeRegistry = {
    addMessageByCompanion(cmp)
  }

  def addFile(file: GeneratedFileObject): TypeRegistry = {
    if (filesSeen.contains(file.scalaDescriptor.fullName)) this
    else {
      val withFileSeen = copy(filesSeen = filesSeen + (file.scalaDescriptor.fullName))

      val withDeps: TypeRegistry =
        file.dependencies.foldLeft(withFileSeen)((r, f) => r.addFile(f))

      file.messagesCompanions.foldLeft(withDeps)((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
    }
  }

  def addMessageByCompanion(cmp: GenericCompanion): TypeRegistry = {
    // TODO: need to add contained file to follow JacksonFormat
    val withNestedMessages =
      cmp.nestedMessagesCompanions.foldLeft(this)((r, mc) => r.addMessageByCompanion(mc.asInstanceOf[GenericCompanion]))
    copy(companions = withNestedMessages.companions + ((TypeRegistry.TypePrefix + cmp.scalaDescriptor.fullName) -> cmp))
  }

  def findType(typeName: String): Option[GenericCompanion] = companions.get(typeName)
}

object Printer {
  final private case class PrinterConfig(
      isIncludingDefaultValueFields: Boolean,
      isPreservingProtoFieldNames: Boolean,
      isFormattingLongAsNumber: Boolean,
      isFormattingEnumsAsNumber: Boolean,
      formatRegistry: FormatRegistry,
      typeRegistry: TypeRegistry)

  private def defaultConfig(implicit mapper: ObjectMapper) =
    PrinterConfig(
      isIncludingDefaultValueFields = false,
      isPreservingProtoFieldNames = false,
      isFormattingLongAsNumber = false,
      isFormattingEnumsAsNumber = false,
      formatRegistry = JacksonFormat.defaultRegistry,
      typeRegistry = TypeRegistry.empty)
}

object TypeRegistry {
  private val TypePrefix = "type.googleapis.com/"

  def empty = TypeRegistry(Map.empty)
}

class Printer private (config: Printer.PrinterConfig)(implicit mapper: ObjectMapper) {
  def this()(implicit mapper: ObjectMapper) = this(Printer.defaultConfig)

  def includingDefaultValueFields: Printer = new Printer(config.copy(isIncludingDefaultValueFields = true))

  def preservingProtoFieldNames: Printer = new Printer(config.copy(isPreservingProtoFieldNames = true))

  def formattingLongAsNumber: Printer = new Printer(config.copy(isFormattingLongAsNumber = true))

  def formattingEnumsAsNumber: Printer = new Printer(config.copy(isFormattingEnumsAsNumber = true))

  def withFormatRegistry(formatRegistry: FormatRegistry): Printer =
    new Printer(config.copy(formatRegistry = formatRegistry))

  def withTypeRegistry(typeRegistry: TypeRegistry): Printer =
    new Printer(config.copy(typeRegistry = typeRegistry))

  def typeRegistry: TypeRegistry = config.typeRegistry

  def print[A](m: GeneratedMessage): String = toJson(m).toString

  private def serializeMessageField(fd: FieldDescriptor, name: String, value: Any, b: ObjectNode)(implicit
      mapper: ObjectMapper): Unit = {
    value match {
      case null =>
      // We are never printing empty optional messages to prevent infinite recursion.
      case Nil =>
        if (config.isIncludingDefaultValueFields) {
          b.set(name, if (fd.isMapField) mapper.createObjectNode() else mapper.createArrayNode())
        }
      case xs: Iterable[GeneratedMessage] @unchecked =>
        if (fd.isMapField) {
          val mapEntryDescriptor = fd.scalaType.asInstanceOf[ScalaType.Message].descriptor
          val keyDescriptor = mapEntryDescriptor.findFieldByNumber(1).get
          val valueDescriptor = mapEntryDescriptor.findFieldByNumber(2).get
          val obj = mapper.createObjectNode()
          xs.foreach { x =>
            val key = x.getField(keyDescriptor) match {
              case PBoolean(v) => v.toString
              case PDouble(v)  => v.toString
              case PFloat(v)   => v.toString
              case PInt(v)     => v.toString
              case PLong(v)    => v.toString
              case PString(v)  => v
              case v           => throw new JsonFormatException(s"Unexpected value for key: $v")
            }
            val value = if (valueDescriptor.protoType.isTypeMessage) {
              toJson(x.getFieldByNumber(valueDescriptor.number).asInstanceOf[GeneratedMessage])
            } else {
              serializeSingleValue(valueDescriptor, x.getField(valueDescriptor), config.isFormattingLongAsNumber)
            }
            obj.set(key, value)
          }
          b.set(name, obj)
        } else {
          val arr = mapper.createArrayNode()
          xs.foreach(value => arr.add(toJson(value)))
          b.set(name, arr)
        }
      case msg: GeneratedMessage =>
        b.set(name, toJson(msg))
      case v =>
        throw new JsonFormatException(v.toString)
    }
  }

  private def serializeNonMessageField(fd: FieldDescriptor, name: String, value: PValue, b: ObjectNode)(implicit
      mapper: ObjectMapper): Unit = {
    value match {
      case PEmpty =>
        if (config.isIncludingDefaultValueFields && fd.containingOneof.isEmpty) {
          b.set(name, defaultJValue(fd))
        }
      case PRepeated(xs) =>
        if (xs.nonEmpty || config.isIncludingDefaultValueFields) {
          val arr = mapper.createArrayNode()
          xs.map(value => arr.add(serializeSingleValue(fd, value, config.isFormattingLongAsNumber)))
          b.set(name, arr)
        }
      case v =>
        if (config.isIncludingDefaultValueFields ||
          !fd.isOptional ||
          !fd.file.isProto3 ||
          (v != JacksonFormat.defaultValue(fd)) ||
          fd.containingOneof.isDefined) {
          b.set(name, serializeSingleValue(fd, v, config.isFormattingLongAsNumber))
        }
    }
  }

  def toJson[A <: GeneratedMessage](m: A)(implicit mapper: ObjectMapper): JsonNode = {
    config.formatRegistry.getMessageWriter[A](m.getClass) match {
      case Some(f) => f(this, m)
      case None =>
        val obj = mapper.createObjectNode()
        val descriptor = m.companion.scalaDescriptor
        descriptor.fields.foreach { f =>
          val name = if (config.isPreservingProtoFieldNames) f.name else JacksonFormat.jsonName(f)
          if (f.protoType.isTypeMessage) {
            serializeMessageField(f, name, m.getFieldByNumber(f.number), obj)
          } else {
            serializeNonMessageField(f, name, m.getField(f), obj)
          }
        }
        obj
    }
  }

  private def defaultJValue(fd: FieldDescriptor): JsonNode =
    serializeSingleValue(fd, JacksonFormat.defaultValue(fd), config.isFormattingLongAsNumber)

  private def unsignedInt(n: Int): Long = n & 0x00000000ffffffffL

  private def unsignedLong(n: Long): BigInt =
    if (n < 0) BigInt(n & 0x7fffffffffffffffL).setBit(63) else BigInt(n)

  private def formatLong(n: Long, protoType: FieldDescriptorProto.Type, formattingLongAsNumber: Boolean): JsonNode = {
    val v: BigInt = if (protoType.isTypeUint64 || protoType.isTypeFixed64) unsignedLong(n) else BigInt(n)
    if (formattingLongAsNumber) new BigIntegerNode(v.bigInteger) else new TextNode(v.toString())
  }

  def serializeSingleValue(fd: FieldDescriptor, value: PValue, formattingLongAsNumber: Boolean): JsonNode =
    value match {
      case PEnum(e) =>
        config.formatRegistry.getEnumWriter(e.containingEnum) match {
          case Some(writer) => writer(this, e)
          case None         => if (config.isFormattingEnumsAsNumber) new IntNode(e.number) else new TextNode(e.name)
        }
      case PInt(v) if fd.protoType.isTypeUint32  => new LongNode(unsignedInt(v))
      case PInt(v) if fd.protoType.isTypeFixed32 => new LongNode(unsignedInt(v))
      case PInt(v)                               => new IntNode(v)
      case PLong(v)                              => formatLong(v, fd.protoType, formattingLongAsNumber)
      case PDouble(v)                            => new DoubleNode(v)
      case PFloat(v) =>
        if (!v.isNaN && !v.isInfinite) new DecimalNode(BigDecimal.decimal(v).bigDecimal) else new DoubleNode(v)
      case PBoolean(v)                         => BooleanNode.valueOf(v)
      case PString(v)                          => new TextNode(v)
      case PByteString(v)                      => new TextNode(Base64Variants.getDefaultVariant.encode(v.toByteArray))
      case _: PMessage | PRepeated(_) | PEmpty => throw new RuntimeException("Should not happen")
    }
}
