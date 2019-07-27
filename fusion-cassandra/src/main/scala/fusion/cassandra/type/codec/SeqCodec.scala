package fusion.cassandra.`type`.codec

import java.nio.ByteBuffer

import com.datastax.oss.driver.api.core.ProtocolVersion
import com.datastax.oss.driver.api.core.`type`.DataType
import com.datastax.oss.driver.api.core.`type`.codec.TypeCodec
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.internal.core.`type`.codec.ParseUtils

import scala.collection.mutable

class SeqCodec[ElementT](cqlType: DataType, elementCodec: TypeCodec[ElementT]) extends TypeCodec[Seq[ElementT]] {
  private val javaType = ScalaGenericType.seqOf(elementCodec.getJavaType())

  override def getJavaType: GenericType[Seq[ElementT]] = javaType

  override def getCqlType: DataType = cqlType

  override def accepts(value: Object): Boolean =
    if (classOf[Seq[_]].isAssignableFrom(value.getClass)) {
      // runtime type ok, now check element type
      val list: Seq[_] = value.asInstanceOf[Seq[_]]
      list.isEmpty || elementCodec.accepts(list.head)
    } else false

  override def encode(value: Seq[ElementT], protocolVersion: ProtocolVersion): ByteBuffer = ???

  override def decode(bytes: ByteBuffer, protocolVersion: ProtocolVersion): Seq[ElementT] = ???

  override def format(value: Seq[ElementT]): String = value match {
    case null => "NULL"
    case _    => value.mkString("[", ",", "]")
  }

  override def parse(value: String): Seq[ElementT] = {
    if (value == null || value.isEmpty || value.equalsIgnoreCase("NULL")) {
      return null
    }

    var idx = ParseUtils.skipSpaces(value, 0)
    if (value.charAt({ idx += 1; idx - 1 }) != '[')
      throw new IllegalArgumentException(
        s"""Cannot parse list value from "$value", at character $idx expecting '[' but got '${value.charAt(idx)}'""")

    idx = ParseUtils.skipSpaces(value, idx)

    if (value.charAt(idx) == ']') {
      return Nil
    }

    val list = mutable.Buffer[ElementT]()
    while (idx < value.length) {
      var n = 0
      try {
        n = ParseUtils.skipCQLValue(value, idx)
      } catch {
        case _: IllegalArgumentException =>
          throw new IllegalArgumentException(
            s"""Cannot parse list value from $value, invalid CQL value at character $idx""")
      }
      list += elementCodec.parse(value.substring(idx, n))
      idx = n
      idx = ParseUtils.skipSpaces(value, idx)
      if (value.charAt(idx) == ']') {
        return list
      }
      if (value.charAt({ idx += 1; idx - 1 }) != ',')
        throw new IllegalArgumentException(
          s"""Cannot parse list value from "$value", at character $idx expecting ',' but got '${value.charAt(idx)}'""")
      idx = ParseUtils.skipSpaces(value, idx)
    }
    throw new IllegalArgumentException(s"""Malformed list value "$value", missing closing ']'""")
  }
}
