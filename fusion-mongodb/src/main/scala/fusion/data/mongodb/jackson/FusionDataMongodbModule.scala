package fusion.data.mongodb.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.module.scala.JacksonModule
import org.bson.types.ObjectId

private object ObjectIdSer extends JsonSerializer[ObjectId] {
  override def serialize(value: ObjectId, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeString(value.toHexString)
  }
}

private object ObjectIdDeser extends JsonDeserializer[ObjectId] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): ObjectId = {
    p.getValueAsString match {
      case null                         => ctxt.handleUnexpectedToken(classOf[ObjectId], p).asInstanceOf[ObjectId]
      case str if ObjectId.isValid(str) => new ObjectId(str)
      case str                          => throw new JsonParseException(p, s"$str 不是有效的ObjectId字符串")
    }
  }
}

private object ObjectIdDeserializer extends Deserializers.Base {
  override def findBeanDeserializer(
      `type`: JavaType,
      config: DeserializationConfig,
      beanDesc: BeanDescription): JsonDeserializer[_] = {
    val rawClass = `type`.getRawClass
    if (OBJECT_ID.isAssignableFrom(rawClass)) ObjectIdDeser
    else super.findBeanDeserializer(`type`, config, beanDesc)
  }
}

private object ObjectIdSerializer extends Serializers.Base {
  override def findSerializer(
      config: SerializationConfig,
      `type`: JavaType,
      beanDesc: BeanDescription): JsonSerializer[_] = {
    val rawClass = `type`.getRawClass
    if (OBJECT_ID.isAssignableFrom(rawClass)) ObjectIdSer
    else super.findSerializer(config, `type`, beanDesc)
  }
}

class FusionDataMongodbModule extends JacksonModule {
  override def getModuleName: String = "FusionDataMongodbModule"
  this += ObjectIdDeserializer
  this += ObjectIdSerializer
}
