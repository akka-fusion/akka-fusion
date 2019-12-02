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

import java.util.concurrent.TimeUnit

import akka.actor.{ Address, AddressFromURIString }
import com.typesafe.config.Config

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._

case class WorkerSettings(
    jobMaxConcurrent: Int,
    healthInterval: FiniteDuration,
    registerDelay: FiniteDuration,
    registerDelayMax: FiniteDuration,
    registerDelayFactor: Double,
    runOnce: Boolean,
    runJobWorkerActor: Option[String],
    runDir: Option[String]) {
  def computeRegisterDelay(delay: FiniteDuration): FiniteDuration = {
    delay match {
      case Duration.Zero => registerDelay
      case _ if delay < registerDelayMax =>
        delay * registerDelayFactor
        FiniteDuration(delay.toNanos, TimeUnit.NANOSECONDS)
      case _ => registerDelay
    }
  }
}

case class SchedulerXSettings(
    namespace: String,
    groupId: String,
    endpoint: String,
    name: String,
    hostname: String,
    port: Int,
    seedNodes: List[Address],
    roles: Set[String],
    worker: WorkerSettings) {
  def isWorker: Boolean = roles(NodeRoles.WORKER)
  def isBroker: Boolean = roles(NodeRoles.BROKER)
}

object SchedulerXSettings {
  def apply(config: Config): SchedulerXSettings = {
    val sc = config.getConfig(Constants.SCHEDULERX)
    val swc = sc.getConfig("worker")
    val name = sc.getString("name")
    val hostname = config.getString("akka.remote.artery.canonical.hostname")
    val port = config.getInt("akka.remote.artery.canonical.port")
    val seedNodes = config.getStringList("akka.cluster.seed-nodes").asScala.map(AddressFromURIString.parse).toList
    val roles = config.getStringList("akka.cluster.roles").asScala.toSet
    new SchedulerXSettings(
      sc.getString("namespace"),
      sc.getString("groupId"),
      sc.getString("endpoint"),
      name,
      hostname,
      port,
      seedNodes,
      roles,
      WorkerSettings(
        swc.getInt("jobMaxConcurrent"),
        swc.getDuration("healthInterval").toScala,
        swc.getDuration("registerDelay").toScala,
        swc.getDuration("registerDelayMax").toScala,
        swc.getDouble("registerDelayFactor"),
        swc.getBoolean("runOnce"),
        if (swc.hasPath("runJobWorkerActor")) Some(swc.getString("runJobWorkerActor")) else None,
        if (swc.hasPath("runDir")) Some(swc.getString("runDir")) else None))
  }
}
