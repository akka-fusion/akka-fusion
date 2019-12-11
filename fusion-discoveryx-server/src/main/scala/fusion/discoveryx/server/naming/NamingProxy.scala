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
import akka.actor.typed.{ ActorRef, Behavior }
import akka.cluster.sharding.typed.ShardingEnvelope
import fusion.discoveryx.model.InstanceReply
import helloscala.common.IntStatus

object NamingProxy {
  val NAME = "namingProxy"

  def apply(shardRegion: ActorRef[ShardingEnvelope[Namings.Command]]): Behavior[Namings.Command] = Behaviors.setup {
    context =>
      Behaviors.receiveMessagePartial {
        case cmd @ Namings.Heartbeat(_, namespace, serviceName) =>
          Namings.NamingServiceKey.entityId(namespace, serviceName) match {
            case Right(entityId) => shardRegion ! ShardingEnvelope(entityId, cmd)
            case Left(errMsg)    => context.log.warn(s"Heartbeat error: $errMsg; cmd: $cmd")
          }
          Behaviors.same
        case cmd: Namings.ServiceCommand =>
          Namings.NamingServiceKey.entityId(cmd.namespace, cmd.serviceName) match {
            case Right(entityId) => shardRegion ! ShardingEnvelope(entityId, cmd)
            case Left(errMsg) =>
              context.log.debug(s"ServiceCommand error: $errMsg; cmd: $cmd")
              cmd.replyTo ! InstanceReply(IntStatus.BAD_REQUEST)
          }
          Behaviors.same
      }
  }
}
