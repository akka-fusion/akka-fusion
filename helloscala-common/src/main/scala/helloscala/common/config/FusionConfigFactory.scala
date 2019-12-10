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

package helloscala.common.config

import akka.actor.AddressFromURIString
import com.typesafe.config.{ Config, ConfigFactory }

import scala.jdk.CollectionConverters._

object FusionConfigFactory {
  def arrangeConfig(originalConfig: Config, internalPath: String): Config =
    arrangeConfig(originalConfig, internalPath, Nil)

  def arrangeConfig(originalConfig: Config, internalPath: String, module: String, modules: String*): Config =
    arrangeConfig(originalConfig, internalPath, module +: modules)

  def arrangeConfig(originalConfig: Config, internalPath: String, modules: Seq[String]): Config = {
    val items =
      if (originalConfig.hasPath(s"$internalPath.config-modules"))
        originalConfig.getStringList(s"$internalPath.config-modules").asScala ++ modules
      else modules
    items.distinct.foldLeft(originalConfig) {
      case (c, "akka") => arrangeAkkaConfig(c, internalPath)
      case (c, module) => arrangeModuleConfig(c, internalPath, module)
    }
  }

  private def arrangeModuleConfig(originalConfig: Config, internalPath: String, module: String): Config = {
    if (originalConfig.hasPath(s"$internalPath.$module")) {
      ConfigFactory
        .parseString(s"$module ${originalConfig.getConfig(s"$internalPath.$module").root().render()}")
        .withFallback(originalConfig)
    } else {
      originalConfig
    }
  }

  private def arrangeAkkaConfig(originalConfig: Config, internalPath: String): Config = {
    val c = arrangeModuleConfig(originalConfig, internalPath, "akka")
    val name = c.getString(s"$internalPath.name")
    val seedNodes = c.getStringList("akka.cluster.seed-nodes").asScala.filterNot(_.isEmpty).map {
      case addr if !addr.startsWith("akka://") =>
        val address = AddressFromURIString.parse(s"akka://$name@$addr")
        require(
          address.system == name,
          s"Cluster ActorSystem name must equals be $name, but seed-node name is invalid, it si $addr.")
        address
      case addr => AddressFromURIString.parse(addr)
    }
    val seedNodesStr = if (seedNodes.isEmpty) "[]" else seedNodes.mkString("[\"", "\", \"", "\"]")
    ConfigFactory.parseString(s"""akka.cluster.seed-nodes = $seedNodesStr""".stripMargin).withFallback(c)
  }
}
