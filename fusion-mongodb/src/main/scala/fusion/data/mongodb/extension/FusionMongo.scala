package fusion.data.mongodb.extension

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import fusion.core.extension.FusionExtension
import fusion.data.mongodb.constant.MongoConstants
import helloscala.common.Configuration
import org.mongodb.scala.{MongoClient, MongoDriverInformation}

import scala.collection.mutable

final class FusionMongo private (val _system: ExtendedActorSystem) extends FusionExtension {
  val mongoClient: MongoClient = createMongoClient(MongoConstants.PATH_ROOT)
  system.registerOnTermination {
    close()
  }

  private val otherMongoClients = mutable.Map.empty[String, MongoClient]

  def lookup(id: String): MongoClient = synchronized {
    id match {
      case MongoConstants.PATH_ROOT => mongoClient
      case _                        => otherMongoClients.getOrElseUpdate(id, createMongoClient(id))
    }
  }

  def registerClient(id: String, mongoClient: MongoClient, replaceExist: Boolean = false): MongoClient = synchronized {
    require(id == MongoConstants.PATH_ROOT, s"id不能为默认Mongodb配置ID，$id == ${MongoConstants.PATH_ROOT}")
    otherMongoClients.get(id).foreach { client =>
      if (!replaceExist) {
        throw new IllegalAccessException(s"id重复，$id == ${MongoConstants.PATH_ROOT}")
      }
      client.close()
      otherMongoClients.remove(id)
    }
    val client = createMongoClient(id)
    otherMongoClients.put(id, client)
    client
  }

  private def createMongoClient(path: String): MongoClient = {
    require(system.settings.config.hasPath(path), s"配置路径不存在，$path")

    val conf: Configuration = Configuration(system).getConfiguration(path)
    if (conf.hasPath("uri")) {
      MongoClient(conf.getString("uri"), getMongoDriverInformation(conf))
    } else {
      throw new IllegalAccessException(s"配置路径内无有效参数，$path")
    }
  }

  private def close(): Unit = {
    mongoClient.close()
    for ((_, client) <- otherMongoClients) {
      client.close()
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

object FusionMongo extends ExtensionId[FusionMongo] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMongo = new FusionMongo(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionMongo
}
