package fusion.data.mongodb.config

import helloscala.common.ConfigLoader
import helloscala.common.ConfigLoader.stringLoader
import org.bson.types.ObjectId

object MongodbConfigLoader {
  implicit val objectIdLoader: ConfigLoader[ObjectId] = stringLoader.map(new ObjectId(_))
  implicit val seqObjectIdLoader: ConfigLoader[Seq[ObjectId]] =
    ConfigLoader.seqStringLoader.map(_.map(str => new ObjectId(str)))
}
