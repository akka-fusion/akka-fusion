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

package fusion.discoveryx.server.naming

import com.typesafe.scalalogging.StrictLogging
import fusion.discoveryx.DiscoveryXUtils
import fusion.discoveryx.model.{ Instance, InstanceHeartbeat, InstanceModify, InstanceQuery }
import fusion.discoveryx.server.protocol.NamingServiceKey

final private[discoveryx] class InternalInstance(private val underlying: Instance, settings: NamingSettings)
    extends Ordered[InternalInstance]
    with Equals {
  @transient private val UNHEALTHY_CHECK_THRESHOLD_MILLIS = settings.heartbeatInterval.toMillis + 2000
  @transient val instanceId: String = underlying.instanceId

  @transient var lastTickTimestamp: Long = System.currentTimeMillis()

  def healthy: Boolean = (System.currentTimeMillis() - lastTickTimestamp) < UNHEALTHY_CHECK_THRESHOLD_MILLIS

  def refresh(): InternalInstance = {
    lastTickTimestamp = System.currentTimeMillis()
    this
  }

  def withInstance(in: Instance): InternalInstance = new InternalInstance(in, settings)

  def toInstance: Instance = underlying.copy(healthy = healthy)

  override def compare(that: InternalInstance): Int = {
    if (that.underlying.weight > underlying.weight) 1
    else if (that.underlying.weight < underlying.weight) -1
    else that.underlying.instanceId.compare(underlying.instanceId)
  }

  override def canEqual(that: Any): Boolean = {
    this == that || (that match {
      case other: InternalInstance => other.underlying.instanceId == underlying.instanceId
      case _                       => false
    })
  }

  override def equals(obj: Any): Boolean = canEqual(obj)

  override def toString =
    s"InternalInstance(${underlying.instanceId}, ${underlying.namespace}, ${underlying.groupName}, ${underlying.serviceName}, ${underlying.ip}, ${underlying.port}, $healthy, $lastTickTimestamp)"
}

final private[discoveryx] class InternalService(namingServiceKey: NamingServiceKey, settings: NamingSettings)
    extends StrictLogging {
  private var curHealthyIdx = 0
  private var instances = Vector[InternalInstance]()
  private var instIds = Map[String, Int]() // instance id, insts index

  def queryInstance(in: InstanceQuery): Vector[Instance] = {
    logger.debug(s"queryInstance($in); curHealthyIdx: $curHealthyIdx; instIds: $instIds; $instances")
    val selects =
      if (in.allHealthy) allHealthy()
      else if (in.oneHealthy) oneHealthy()
      else allInstance()
    selects.map(_.toInstance)
  }

  def addInstance(inst: Instance): InternalService = {
    val items = instIds.get(inst.instanceId) match {
      case Some(idx) => instances.updated(idx, new InternalInstance(inst, settings))
      case None      => new InternalInstance(inst, settings) +: instances
    }
    saveInstances(items)
    logger.debug(s"addInstance($inst) after; curHealthyIdx: $curHealthyIdx; instIds: $instIds; $instances")
    this
  }

  def modifyInstance(in: InstanceModify): Option[Instance] = {
    val instId = DiscoveryXUtils.makeInstanceId(in.serviceName, in.serviceName, in.ip, in.port)
    instIds.get(instId).map { idx =>
      val internal = instances(idx)
      val older = internal.toInstance
      val newest = older.copy(
        weight = if (in.weight > 0.0) in.weight else older.weight,
        healthy = in.healthy,
        enabled = in.enabled,
        metadata = in.metadata)
      saveInstances(instances.updated(idx, internal.withInstance(newest)))
      newest
    }
  }

  def removeInstance(instId: String): Boolean = {
    if (instIds.contains(instId)) {
      saveInstances(instances.filterNot(_.instanceId == instId))
      true
    } else {
      false
    }
  }

  def allInstance(): Vector[InternalInstance] = instances

  def allHealthy(): Vector[InternalInstance] = instances.filter(_.healthy)

  def oneHealthy(): Vector[InternalInstance] = {
    val healths = allHealthy()
    if (healths.isEmpty) {
      curHealthyIdx = 0
      healths
    } else if (curHealthyIdx < healths.size) {
      val ret = healths(curHealthyIdx)
      curHealthyIdx += 1
      Vector(ret)
    } else {
      curHealthyIdx = if (healths.size == 1) 0 else 1
      Vector(healths.head)
    }
  }

  def processHeartbeat(in: InstanceHeartbeat): InternalService = {
    instIds.get(in.instanceId) match {
      case Some(idx) =>
        val inst = instances(idx).refresh()
        logger.debug(s"Process heartbeat success, in: $in, instance: $inst")
      case None => logger.warn(s"服务未注册，${in.instanceId}")
    }
    this
  }

  def checkHealthy(): InternalService = {
    for (inst <- instances if !inst.healthy) {
      // TODO alarm or remove unhealthy instance ?
      logger.warn(s"Instance unhealthy, is $inst")
    }
    this
  }

  private def saveInstances(items: Vector[InternalInstance]): Unit = {
    this.instances = items.sortWith(_ > _)
    instIds = this.instances.view.map(_.instanceId).zipWithIndex.toMap
  }
}
