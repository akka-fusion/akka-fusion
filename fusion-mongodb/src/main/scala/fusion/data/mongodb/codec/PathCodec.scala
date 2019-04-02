package fusion.data.mongodb.codec

import java.nio.file.Path
import java.nio.file.Paths

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

// XXX Path是一个 Iterable[Path]，会造成write时被单成数组而造成无限循环
class PathCodec extends Codec[Path] {
  override def decode(reader: BsonReader, decoderContext: DecoderContext): Path =
    Paths.get(reader.readString())

  override def encode(writer: BsonWriter, value: Path, encoderContext: EncoderContext): Unit =
    writer.writeString(value.toString)

  override def getEncoderClass: Class[Path] = classOf[Path]
}
