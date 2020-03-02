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

package fusion.common.config

import akka.actor.AddressFromURIString
import com.typesafe.config.{ Config, ConfigFactory }
import helloscala.common.Configuration
import helloscala.common.exception.HSConfigurationException

import scala.jdk.CollectionConverters._

object FusionConfigFactory {
  def arrangeConfig(originalConfig: Config): Config = {
    if (originalConfig.hasPath("fusion.configuration")) {
      val c = Configuration(originalConfig.getConfig("fusion.configuration"))
      arrangeConfig(originalConfig, c.keys.head)
    } else {
      originalConfig
    }
  }

  def arrangeConfig(internalPath: String, modules: Seq[String]): Config =
    arrangeConfig(ConfigFactory.load(), internalPath, modules)

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

  private def arrangeModuleConfig(c: Config, internalPath: String, module: String): Config = {
    if (c.hasPath(s"$internalPath.$module")) {
      val inner = c.getConfig(s"$internalPath.$module").root().render()
      ConfigFactory.parseString(s"$module $inner").withFallback(c)
    } else c
  }

  private def arrangeAkkaConfig(originalConfig: Config, internalPath: String): Config = {
    val c = arrangeModuleConfig(originalConfig, internalPath, "akka")
    val name =
      if (c.hasPath(s"$internalPath.akka-name")) c.getString(s"$internalPath.akka-name")
      else if (c.hasPath(s"$internalPath.name")) c.getString(s"$internalPath.name")
      else
        throw HSConfigurationException(
          s"Configuration key '$internalPath.akka-name' or '$internalPath.name' not found.")

    val seedNodes = c.getStringList("akka.cluster.seed-nodes").asScala.filterNot(_.isEmpty).map {
      case addr if !addr.startsWith("akka://") => AddressFromURIString.parse(s"akka://$name@$addr")
      case addr                                => AddressFromURIString.parse(addr)
    }

    val seedNodesStr = if (seedNodes.isEmpty) "[]" else seedNodes.mkString("[\"", "\", \"", "\"]")
    ConfigFactory.parseString(s"""akka.cluster.seed-nodes = $seedNodesStr""".stripMargin).withFallback(c)
  }
}
