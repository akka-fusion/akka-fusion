package fusion.kafka.commn.serialization

import java.util

import org.apache.kafka.common.serialization.Deserializer

class JacksonDeserializer extends Deserializer[String] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = ???
  override def deserialize(topic: String, data: Array[Byte]): String         = ???
  override def close(): Unit                                                 = ???
}
