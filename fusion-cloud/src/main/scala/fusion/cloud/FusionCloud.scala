/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.cloud

import akka.actor.typed.{ActorSystem, Extension, ExtensionId}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.util.Timeout
import com.typesafe.config.Config
import fusion.cloud.FusionCloud.Constants
import fusion.cloud.config.FusionCloudConfig
import fusion.cloud.discovery.FusionCloudDiscovery

import scala.concurrent.duration._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-02 13:34:38
 */
class FusionCloud(val system: ActorSystem[_]) extends Extension {
  implicit val timeout: Timeout = 5.seconds
  val entityRefs = new EntityRefs(system)

  val cloudConfig: FusionCloudConfig = system.dynamicAccess
    .getObjectFor[ExtensionId[_ <: FusionCloudConfig]](config.getString(Constants.CONFIG_CLASS))
    .getOrElse(throw new ExceptionInInitializerError(s"Configuration path '${Constants.CONFIG_CLASS}' is not exists."))
    .apply(system)

  val cloudDiscovery: FusionCloudDiscovery = system.dynamicAccess
    .getObjectFor[ExtensionId[_ <: FusionCloudDiscovery]](config.getString(Constants.DISCOVERY_CLASS))
    .getOrElse(
      throw new ExceptionInInitializerError(s"Configuration path '${Constants.DISCOVERY_CLASS}' is not exists.")
    )
    .apply(system)
  def config: Config = system.settings.config
}

object FusionCloud extends ExtensionId[FusionCloud] {

  object Constants {
    val CONFIG_CLASS = "fusion.cloud.config-class"
    val DISCOVERY_CLASS = "fusion.cloud.discovery-class"
  }
  override def createExtension(system: ActorSystem[_]): FusionCloud = new FusionCloud(system)
}

class EntityRefs(system: ActorSystem[_]) {

  def cloudDiscovery: EntityRef[FusionCloudDiscovery.Command] =
    ClusterSharding(system).entityRefFor(FusionCloudDiscovery.TypeKey, EntityIds.entityId(system))
}
