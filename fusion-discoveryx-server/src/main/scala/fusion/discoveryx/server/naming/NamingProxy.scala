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

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior, SupervisorStrategy }
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, Entity }
import fusion.common.FusionActorRefFactory
import fusion.discoveryx.model.InstanceReply
import fusion.discoveryx.server.protocol._
import helloscala.common.IntStatus
import scala.concurrent.duration._

object NamingProxy {
  val NAME = "namingProxy"

  def create(fusionActorRefFactory: FusionActorRefFactory): ActorRef[Namings.Command] = {
    fusionActorRefFactory.spawnActorSync(
      Behaviors.supervise(NamingProxy()).onFailure(SupervisorStrategy.restart),
      NAME,
      2.seconds)
  }

  def apply(): Behavior[Namings.Command] = Behaviors.setup { context =>
    val shardRegion =
      ClusterSharding(context.system).init(Entity(Namings.TypeKey)(entityContext => Namings(entityContext.entityId)))

    def send(cmd: Namings.Command, namespace: String, serviceName: String): Behavior[Namings.Command] = {
      Namings.NamingServiceKey.entityId(namespace, serviceName) match {
        case Right(entityId) => shardRegion ! ShardingEnvelope(entityId, cmd)
        case Left(errMsg)    => context.log.error(s"ReplyCommand error: $errMsg; cmd: $cmd")
      }
      Behaviors.same
    }
    def sendReply(cmd: Namings.ReplyCommand, namespace: String, serviceName: String): Behavior[Namings.Command] = {
      Namings.NamingServiceKey.entityId(namespace, serviceName) match {
        case Right(entityId) => shardRegion ! ShardingEnvelope(entityId, cmd)
        case Left(errMsg) =>
          context.log.error(s"ReplyCommand error: $errMsg; cmd: $cmd")
          cmd.replyTo ! InstanceReply(IntStatus.BAD_REQUEST)
      }
      Behaviors.same
    }

    Behaviors.receiveMessagePartial {
      case cmd: Heartbeat        => send(cmd, cmd.namespace, cmd.serviceName)
      case cmd: RegisterInstance => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
      case cmd: RemoveInstance   => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
      case cmd: ModifyInstance   => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
      case cmd: QueryInstance    => sendReply(cmd, cmd.in.namespace, cmd.in.serviceName)
    }
  }
}
