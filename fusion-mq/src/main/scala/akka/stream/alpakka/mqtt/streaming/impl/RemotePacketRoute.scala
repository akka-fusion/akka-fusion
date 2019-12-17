/*
 * Copyright 2019 akka-fusion.com
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

package akka.stream.alpakka.mqtt.streaming.impl

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ ActorRef, Behavior }
import akka.annotation.InternalApi
import akka.stream.alpakka.mqtt.streaming._
import akka.util.ByteString

import scala.concurrent.Promise
import scala.util.control.NoStackTrace

@InternalApi private[streaming] object RemotePacketRouter {
  /*
   * Raised on routing if a packet id cannot determine an actor to route to
   */
  case class CannotRoute(packetId: PacketId) extends Exception("packet id: " + packetId.underlying) with NoStackTrace

  // Requests

  sealed abstract class Request[A]
  final case class Register[A](
      registrant: ActorRef[A],
      clientId: Option[String],
      packetId: PacketId,
      reply: Promise[Registered.type])
      extends Request[A]
  final case class RegisterConnection[A](connectionId: ByteString, clientId: String) extends Request[A]
  private final case class Unregister[A](clientId: Option[String], packetId: PacketId) extends Request[A]
  final case class UnregisterConnection[A](connectionId: ByteString) extends Request[A]
  final case class Route[A](clientId: Option[String], packetId: PacketId, event: A, failureReply: Promise[_])
      extends Request[A]
  final case class RouteViaConnection[A](
      connectionId: ByteString,
      packetId: PacketId,
      event: A,
      failureReply: Promise[_])
      extends Request[A]

  // Replies

  sealed abstract class Reply
  final case object Registered extends Reply

  /*
   * Construct with the starting state
   */
  def apply[A]: Behavior[Request[A]] =
    new RemotePacketRouter[A].main(Map.empty, Map.empty)

  private[streaming] case class Registration[A](registrant: ActorRef[A], failureReplies: Seq[Promise[_]])
}

/*
 * Route remotely generated MQTT packets based on packet identifiers.
 * Callers are able to request that they be registered for routing
 * along with a packet id received from the remote. These
 * callers are then watched for termination so that housekeeping can
 * be performed. The contract is therefore for a caller to initially register,
 * and to terminate when it has finished with the packet identifier.
 */
@InternalApi private[streaming] final class RemotePacketRouter[A] {
  import RemotePacketRouter._

  // Processing

  def main(
      registrantsByPacketId: Map[(Option[String], PacketId), Registration[A]],
      clientIdsByConnectionId: Map[ByteString, String]): Behavior[Request[A]] =
    Behaviors.receive {
      case (context, Register(registrant: ActorRef[A], clientId, packetId, reply)) =>
        reply.success(Registered)
        context.watchWith(registrant, Unregister(clientId, packetId))
        val key = (clientId, packetId)
        main(registrantsByPacketId + (key -> Registration(registrant, List.empty)), clientIdsByConnectionId)
      case (_, RegisterConnection(connectionId, clientId)) =>
        main(registrantsByPacketId, clientIdsByConnectionId + (connectionId -> clientId))
      case (_, Unregister(clientId, packetId)) =>
        // We tidy up and fail any failure promises that haven't already been failed -
        // just in case the registrant terminated abnormally and didn't get to complete
        // the promise. We all know that uncompleted promises can lead to memory leaks.
        // The known condition by which we'd succeed in failing the promise here is
        // when we thought we were able to route to a registrant, but the routing
        // subsequently failed, ending up the in the deadletter queue.
        registrantsByPacketId.get((clientId, packetId)).toList.flatMap(_.failureReplies).foreach { failureReply =>
          failureReply.tryFailure(CannotRoute(packetId))
        }
        val key = (clientId, packetId)
        main(registrantsByPacketId - key, clientIdsByConnectionId)
      case (_, UnregisterConnection(connectionId)) =>
        main(registrantsByPacketId, clientIdsByConnectionId - connectionId)
      case (_, Route(clientId, packetId, event, failureReply)) =>
        val key = (clientId, packetId)
        registrantsByPacketId.get(key) match {
          case Some(registration) =>
            registration.registrant ! event
            main(
              registrantsByPacketId.updated(
                (clientId, packetId),
                registration.copy(failureReplies = failureReply +: registration.failureReplies)),
              clientIdsByConnectionId)
          case None =>
            failureReply.failure(CannotRoute(packetId))
            Behaviors.same
        }
      case (_, RouteViaConnection(connectionId, packetId, event, failureReply)) =>
        clientIdsByConnectionId.get(connectionId) match {
          case clientId: Some[String] =>
            val key = (clientId, packetId)
            registrantsByPacketId.get(key) match {
              case Some(registration) =>
                registration.registrant ! event
                main(
                  registrantsByPacketId.updated(
                    (clientId, packetId),
                    registration.copy(failureReplies = failureReply +: registration.failureReplies)),
                  clientIdsByConnectionId)
              case None =>
                failureReply.failure(CannotRoute(packetId))
                Behaviors.same
            }
          case None =>
            failureReply.failure(CannotRoute(packetId))
            Behaviors.same
        }
    }
}
