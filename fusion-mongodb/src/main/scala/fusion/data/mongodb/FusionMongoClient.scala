package fusion.data.mongodb

import akka.actor.ActorSystem
import fusion.data.mongodb.constant.MongoConstants
import helloscala.common.Configuration
import org.mongodb.scala.MongoClient

class FusionMongoClient(system: ActorSystem) {
  val conf: Configuration = Configuration(system).getConfiguration(MongoConstants.PATH_ROOT)
  val client: MongoClient = createMongoClient()
  system.registerOnTermination {
    client.close()
  }

  private def createMongoClient(): MongoClient = {
    if (conf.hasPath("uri")) {
      MongoClient(conf.getString("uri"))
    } else {
      MongoClient()
    }
  }
}
