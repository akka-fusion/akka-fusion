/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.data.mongodb.extension

import akka.Done
import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.MongoDriverInformation
import com.mongodb.reactivestreams.client.MongoClients
import fusion.core.component.Components
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.data.mongodb.MongoTemplate
import fusion.data.mongodb.constant.MongoConstants
import helloscala.common.Configuration

import scala.concurrent.Future

final private[mongodb] class MongoComponents(system: ActorSystem)
    extends Components[MongoTemplate](MongoConstants.PATH_DEFAULT) {
  import system.dispatcher
  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def componentClose(c: MongoTemplate): Future[Done] = Future {
    c.close()
    Done
  }

  override protected def createComponent(id: String): MongoTemplate = {
    require(system.settings.config.hasPath(id), s"配置路径不存在，$id")

    val c: Configuration = configuration.getConfiguration(id)
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
  FusionCore(system)
  val components = new MongoComponents(system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionMongo") { () =>
    components.closeAsync()(system.dispatcher)
  }

  def mongoTemplate: MongoTemplate = components.component
}

object FusionMongo extends ExtensionId[FusionMongo] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionMongo = new FusionMongo(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionMongo
}
