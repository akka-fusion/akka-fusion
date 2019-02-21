package fusion.data.mongodb.extension

import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.mongodb.{ConnectionString, MongoClientSettings}
import com.typesafe.config.Config
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.data.mongodb.MongoTemplate
import fusion.data.mongodb.constant.MongoConstants
import helloscala.common.Configuration
import org.mongodb.scala.{MongoClient, MongoDriverInformation}

final private[mongodb] class MongoComponents(system: ActorSystem)
    extends Components[MongoTemplate](MongoConstants.PATH_DEFAULT) {
  override def config: Config = system.settings.config

  override protected def componentClose(c: MongoTemplate): Unit = c.close()

  override protected def createComponent(path: String): MongoTemplate = {
    require(system.settings.config.hasPath(path), s"配置路径不存在，$path")

    val conf: Configuration = Configuration(system).getConfiguration(path)
    if (conf.hasPath("uri")) {
      val settings = MongoClientSettings
        .builder()
        .applyConnectionString(new ConnectionString(conf.getString("uri")))
        .codecRegistry(MongoTemplate.DEFAULT_CODEC_REGISTRY)
        .build()
      MongoTemplate(MongoClient(settings, getMongoDriverInformation(conf)))
    } else {
      throw new IllegalAccessException(s"配置路径内无有效参数，$path")
    }
  }

  private def getMongoDriverInformation(conf: Configuration): Option[MongoDriverInformation] = {
    val maybeDriverName = conf.get[Option[String]]("driverName")
    val maybeDriverVersion = conf.get[Option[String]]("driverVersion")
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
  val components = new MongoComponents(system)
  system.registerOnTermination {
    components.close()
  }

  def mongoTemplate: MongoTemplate = components.component
}

object FusionMongo extends ExtensionId[FusionMongo] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMongo = new FusionMongo(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionMongo
}
