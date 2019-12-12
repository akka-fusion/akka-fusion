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

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Terminated }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity, EntityTypeKey }
import akka.util.Timeout
import fusion.discoveryx.model.InstanceReply
import fusion.discoveryx.server.protocol._
import fusion.json.jackson.CborSerializable
import helloscala.common.IntStatus

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object NamingManager {
  trait Command extends Namings.Command
  trait Reply

  val NAME = "namingProxy"
  val TypeKey: EntityTypeKey[Namings.Command] = EntityTypeKey("NamingManager")

  def init(system: ActorSystem[_]): ActorRef[ShardingEnvelope[Namings.Command]] = {
    ClusterSharding(system).init(Entity(TypeKey)(entityContext => NamingManager(entityContext.entityId)))
  }

  def apply(namespace: String): Behavior[Namings.Command] =
    Behaviors.setup(context => new NamingManager(namespace, context).receive())
}

class NamingManager private (namespace: String, context: ActorContext[Namings.Command]) {
  import NamingManager._
  private implicit val system: ActorSystem[_] = context.system
  private val namingSettings = NamingSettings(context.system)
  private val shardRegion =
    ClusterSharding(context.system).init(Entity(Namings.TypeKey)(entityContext => Namings(entityContext.entityId)))
  private var namings = List.empty[ActorRef[Namings.Command]]

  def receive(): Behavior[Namings.Command] =
    Behaviors
      .receiveMessage[Namings.Command] {
        case cmd: Heartbeat                     => send(cmd, cmd.namespace, cmd.serviceName)
        case cmd: RegisterInstance              => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
        case cmd: RemoveInstance                => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
        case cmd: ModifyInstance                => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
        case cmd: QueryInstance                 => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
        case NamingManagerCommand(replyTo, cmd) => onManagerCommand(cmd, replyTo)
        case NamingRegisterToManager(namingRef) =>
          namings = namingRef :: namings.filterNot(old => old.path.name == namingRef.path.name)
          context.watch(namingRef)
          Behaviors.same
      }
      .receiveSignal {
        case (_, Terminated(ref)) =>
          namings = namings.filterNot(_ == ref)
          Behaviors.same
      }

  private def onManagerCommand(command: NamingManagerCommand.Cmd, replyTo: ActorRef[Reply]): Behavior[Namings.Command] =
    command match {
      case NamingManagerCommand.Cmd.ListService(cmd) => processListService(cmd, replyTo)
      case other =>
        context.log.warn(s"Invalid message: $other")
        Behaviors.same
    }

  import akka.actor.typed.scaladsl.AskPattern._
  import context.executionContext
  implicit val timeout: Timeout = 10.seconds

  private def processListService(cmd: ListService, replyTo: ActorRef[Reply]): Behavior[Namings.Command] = {
    val page = namingSettings.findPage(cmd.page)
    val size = namingSettings.findSize(cmd.size)
    val offset = (page - 1) * size
    if (offset < namings.size) {
      val futures = namings.slice(offset, offset + size).map { naming =>
        naming.ask[ServiceInfo](ref => QueryServiceInfo(ref))
      }
      Future.sequence(futures).onComplete {
        case Success(serviceInfos) =>
          replyTo ! NamingResponse(IntStatus.OK, NamingResponse.Data.ServiceInfos(ListedService(serviceInfos)))
        case Failure(exception) =>
          context.log.warn(s"ListService error, $exception")
          replyTo ! NamingResponse(IntStatus.NOT_FOUND)
      }
    } else {
      replyTo ! NamingResponse(IntStatus.NOT_FOUND)
    }

    Behaviors.same
  }

  private def send(cmd: Namings.Command, namespace: String, serviceName: String): Behavior[Namings.Command] = {
    Namings.NamingServiceKey.entityId(namespace, serviceName) match {
      case Right(entityId) => shardRegion ! ShardingEnvelope(entityId, cmd)
      case Left(errMsg)    => context.log.error(s"ReplyCommand error: $errMsg; cmd: $cmd")
    }
    Behaviors.same
  }

  private def sendReply(
      cmd: Namings.ReplyCommand,
      namespace: String,
      serviceName: String): Behavior[Namings.Command] = {
    Namings.NamingServiceKey.entityId(namespace, serviceName) match {
      case Right(entityId) => shardRegion ! ShardingEnvelope(entityId, cmd)
      case Left(errMsg) =>
        context.log.error(s"ReplyCommand error: $errMsg; cmd: $cmd")
        cmd.replyTo ! InstanceReply(IntStatus.BAD_REQUEST)
    }
    Behaviors.same
  }
}
