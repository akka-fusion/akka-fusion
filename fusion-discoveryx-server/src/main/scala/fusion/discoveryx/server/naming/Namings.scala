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

import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors, TimerScheduler }
import akka.actor.typed.{ ActorRef, Behavior }
import akka.cluster.sharding.typed.scaladsl.EntityTypeKey
import fusion.discoveryx.DiscoveryXUtils
import fusion.discoveryx.model._
import fusion.discoveryx.server.protocol._
import helloscala.common.IntStatus
import helloscala.common.exception.HSBadRequestException
import helloscala.common.util.StringUtils

object Namings {
  val TypeKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Naming")

  trait Command
  trait ReplyCommand extends Command {
    @transient val replyTo: ActorRef[InstanceReply]
  }

  private case object HealthCheckKey extends Command

  object NamingServiceKey {
    def entityId(namespace: String, serviceName: String): Either[String, String] = {
      if (StringUtils.isBlank(namespace) || StringUtils.isBlank(serviceName)) {
        Left("entityId invalid, need [namespace]_[serviceName] format.")
      } else {
        Right(s"${namespace}_$serviceName")
      }
    }

    def unapply(entityId: String): Option[NamingServiceKey] = entityId.split('_') match {
      case Array(namespace, serviceName) => Some(new NamingServiceKey(namespace, serviceName))
      case _                             => None
    }
  }

  def apply(entityId: String): Behavior[Command] = Behaviors.setup[Command] { context =>
    val namingServiceKey = NamingServiceKey
      .unapply(entityId)
      .getOrElse(throw HSBadRequestException(
        s"${context.self} create child error. entityId invalid, need [namespace]_[serviceName] format."))
    Behaviors.withTimers(timers => new Namings(namingServiceKey, timers, context))
  }
}

class Namings private (
    namingServiceKey: NamingServiceKey,
    timers: TimerScheduler[Namings.Command],
    override protected val context: ActorContext[Namings.Command])
    extends AbstractBehavior[Namings.Command](context) {
  import Namings._
  private val settings = NamingSettings(context.system)
  private val internalService = new InternalService(namingServiceKey, settings)

  timers.startTimerWithFixedDelay(HealthCheckKey, HealthCheckKey, settings.heartbeatInterval)
  context.log.debug(s"Namings started: $namingServiceKey")

  override def onMessage(msg: Namings.Command): Behavior[Namings.Command] = msg match {
    case Heartbeat(in, namespace, serviceName) => processHeartbeat(in, namespace, serviceName)
    case QueryInstance(in, replyTo)            => queryInstance(in, replyTo)
    case RegisterInstance(in, replyTo)         => registerInstance(in, replyTo)
    case RemoveInstance(in, replyTo)           => removeInstance(in, replyTo)
    case ModifyInstance(in, replyTo)           => modifyInstance(in, replyTo)
    case HealthCheckKey                        => healthCheck()
  }

  private def healthCheck(): Namings = {
    internalService.checkHealthy()
    this
  }

  private def processHeartbeat(in: InstanceHeartbeat, namespace: String, serviceName: String): Namings = {
    internalService.processHeartbeat(in)
    this
  }

  private def queryInstance(in: InstanceQuery, replyTo: ActorRef[InstanceReply]): Namings = {
    val result = try {
      val items = internalService.queryInstance(in)
      val status = if (items.isEmpty) IntStatus.NOT_FOUND else IntStatus.OK
      InstanceReply(status, InstanceReply.Data.Queried(InstanceQueryResult(items)))
    } catch {
      case _: IllegalArgumentException => InstanceReply(IntStatus.BAD_REQUEST)
    }
    replyTo ! result
    this
  }

  private def modifyInstance(in: InstanceModify, replyTo: ActorRef[InstanceReply]): Namings = {
    val result = try {
      internalService.modifyInstance(in) match {
        case Some(_) => InstanceReply(IntStatus.OK)
        case None    => InstanceReply(IntStatus.NOT_FOUND)
      }
    } catch {
      case _: IllegalArgumentException => InstanceReply(IntStatus.BAD_REQUEST)
    }
    replyTo ! result
    this
  }

  private def removeInstance(in: InstanceRemove, replyTo: ActorRef[InstanceReply]): Namings = {
    val instId = DiscoveryXUtils.makeInstanceId(in.namespace, in.serviceName, in.ip, in.port)
    val status = if (internalService.removeInstance(instId)) IntStatus.OK else IntStatus.NOT_FOUND
    replyTo ! InstanceReply(status)
    this
  }

  private def registerInstance(in: InstanceRegister, replyTo: ActorRef[InstanceReply]): Namings = {
    val result = try {
      val inst = DiscoveryXUtils.toInstance(in)
      internalService.addInstance(inst)
      InstanceReply(IntStatus.OK, InstanceReply.Data.Registered(inst))
    } catch {
      case _: IllegalArgumentException => InstanceReply(IntStatus.BAD_REQUEST)
    }
    replyTo ! result
    this
  }
}
