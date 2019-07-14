package fusion.data.mongodb.extension

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoDriverInformation
import com.typesafe.config.Config
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.data.mongodb.MongoTemplate
import fusion.data.mongodb.constant.MongoConstants
import helloscala.common.Configuration

final private[mongodb] class MongoComponents(system: ActorSystem)
    extends Components[MongoTemplate](MongoConstants.PATH_DEFAULT) {
  override def config: Configuration = Configuration(system.settings.config)

  override protected def componentClose(c: MongoTemplate): Unit = c.close()

  override protected def createComponent(id: String): MongoTemplate = {
    require(system.settings.config.hasPath(id), s"配置路径不存在，$id")

    val c: Configuration = config.getConfiguration(id)
    if (c.hasPath("uri")) {
      val connectionString = new ConnectionString(c.getString("uri"))
      val settings = MongoClientSettings
        .builder()
        .applyConnectionString(connectionString)
        .codecRegistry(MongoTemplate.DEFAULT_CODEC_REGISTRY)
        .build()
      val mongoClient =
        getMongoDriverInformation(c).map(MongoClients.create(settings, _)).getOrElse(MongoClients.create(settings))
      MongoTemplate(mongoClient, connectionString.getDatabase)
    } else {
      throw new IllegalAccessException(s"配置路径内无有效参数，$id")
    }
  }

  private def getMongoDriverInformation(conf: Configuration): Option[MongoDriverInformation] = {
    val maybeDriverName     = conf.get[Option[String]]("driverName")
    val maybeDriverVersion  = conf.get[Option[String]]("driverVersion")
    val maybeDriverPlatform = conf.get[Option[String]]("driverPlatform")
    if (maybeDriverName.isDefined || maybeDriverVersion.isDefined || maybeDriverPlatform.isDefined) {
      val builder = MongoDriverInformation.builder()
      maybeDriverName.foreach(builder.driverName)
      maybeDriverVersion.foreach(builder.driverVersion)
      maybeDriverPlatform.foreach(builder.driverPlatform)
      Some(builder.build())
    } else {
      None
    }
  }

}

final class FusionMongo private (val _system: ExtendedActorSystem) extends FusionExtension {
  FusionCore(system)
  val components = new MongoComponents(system)
  system.registerOnTermination {
    components.close()
  }

  def mongoTemplate: MongoTemplate = components.component
}

object FusionMongo extends ExtensionId[FusionMongo] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMongo = new FusionMongo(system)
  override def lookup(): ExtensionId[_ <: Extension]                     = FusionMongo
}
