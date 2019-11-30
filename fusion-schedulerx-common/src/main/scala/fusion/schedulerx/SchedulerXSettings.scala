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

package fusion.schedulerx

import akka.actor.{ Address, AddressFromURIString }
import com.typesafe.config.{ Config, ConfigFactory }

import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._

case class WorkerSettings(healthInterval: FiniteDuration)

case class SchedulerXSettings(
    namespace: String,
    groupId: String,
    endpoint: String,
    name: String,
    host: String,
    port: Int,
    seedNodes: List[Address],
    roles: Set[String],
    worker: WorkerSettings,
    config: Config) {
  def isWorker: Boolean = roles(NodeRoles.WORKER)
  def isBroker: Boolean = roles(NodeRoles.BROKER)
}

object SchedulerXSettings {
  def arrangeConfig(originalConfig: Config): Config = {
    ConfigFactory
      .parseString(s"akka ${originalConfig.getConfig(s"${Constants.SCHEDULERX}.akka").root().render()}")
      .withFallback(originalConfig)
  }

  def apply(): SchedulerXSettings = apply(ConfigFactory.load())

  def apply(originalConfig: Config): SchedulerXSettings = {
    val c = arrangeConfig(originalConfig)
    val sc = c.getConfig(Constants.SCHEDULERX)
    val swc = sc.getConfig("worker")
    val name = sc.getString("name")
    val host = c.getString("akka.remote.artery.canonical.hostname")
    val port = c.getInt("akka.remote.artery.canonical.port")
    val seedNodes = c
      .getStringList("akka.cluster.seed-nodes")
      .asScala
      .map {
        case addr if !addr.startsWith("akka://") =>
          val address = AddressFromURIString.parse(s"akka://$name@$addr")
          require(
            address.system == name,
            s"Cluster ActorSystem name must equals be $name, but seed-node name is invalid, it si $addr.")
          address
        case addr => AddressFromURIString.parse(addr)
      }
      .toList
    val roles = c.getStringList("akka.cluster.roles").asScala.toSet
    new SchedulerXSettings(
      sc.getString("namespace"),
      sc.getString("groupId"),
      sc.getString("endpoint"),
      name,
      host,
      port,
      seedNodes,
      roles,
      WorkerSettings(swc.getDuration("healthInterval").toScala),
      ConfigFactory.parseString(s"""
             |akka.cluster.seed-nodes = ${seedNodes.mkString("[\"", "\", \"", "\"]")}
             |""".stripMargin).withFallback(c))
  }
}
