package fusion.data.mongodb.codec

import org.bson.codecs.Codec
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry

class FusionCodecProvider extends CodecProvider {
  private val codecs: Map[Class[_], Codec[_]] = Map(
//    putCodec(new PathCodec)
  )

  private def putCodec(codec: Codec[_]): (Class[_], Codec[_]) = codec.getEncoderClass -> codec

  override def get[T](clazz: Class[T], registry: CodecRegistry): Codec[T] =
    codecs.get(clazz) match {
      case Some(value) =>
        value.asInstanceOf[Codec[T]]
      case _ =>
        null
    }
}
