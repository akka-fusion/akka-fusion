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

package akka.stream.alpakka.mqtt.streaming.impl

import akka.Done
import akka.actor.typed._
import akka.actor.typed.scaladsl.Behaviors
import akka.annotation.InternalApi
import akka.stream.alpakka.mqtt.streaming._

import scala.concurrent.Promise
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success }

/*
 * A unpublisher manages the client state in relation to unsubscribing from a
 * server-side topic. A unpublisher is created per server per topic.
 */
@InternalApi private[streaming] object Unpublisher {
  /*
   * No ACK received - the unsubscription failed
   */
  case object UnsubscribeFailed extends Exception with NoStackTrace

  /*
   * Construct with the starting state
   */
  def apply(
      clientId: String,
      packetId: PacketId,
      local: Promise[ForwardUnsubscribe.type],
      unsubscribed: Promise[Done],
      packetRouter: ActorRef[RemotePacketRouter.Request[Event]],
      settings: MqttSessionSettings): Behavior[Event] =
    prepareServerUnpublisher(Start(Some(clientId), packetId, local, unsubscribed, packetRouter, settings))

  // Our FSM data, FSM events and commands emitted by the FSM

  sealed abstract class Data(
      val clientId: Some[String],
      val packetId: PacketId,
      val unsubscribed: Promise[Done],
      val packetRouter: ActorRef[RemotePacketRouter.Request[Event]],
      val settings: MqttSessionSettings)
  final case class Start(
      override val clientId: Some[String],
      override val packetId: PacketId,
      local: Promise[ForwardUnsubscribe.type],
      override val unsubscribed: Promise[Done],
      override val packetRouter: ActorRef[RemotePacketRouter.Request[Event]],
      override val settings: MqttSessionSettings)
      extends Data(clientId, packetId, unsubscribed, packetRouter, settings)
  final case class ServerUnsubscribe(
      override val clientId: Some[String],
      override val packetId: PacketId,
      override val unsubscribed: Promise[Done],
      override val packetRouter: ActorRef[RemotePacketRouter.Request[Event]],
      override val settings: MqttSessionSettings)
      extends Data(clientId, packetId, unsubscribed, packetRouter, settings)

  sealed abstract class Event
  final case object RegisteredPacketId extends Event
  final case object UnobtainablePacketId extends Event
  final case class UnsubAckReceivedLocally(remote: Promise[ForwardUnsubAck.type]) extends Event
  case object ReceiveUnsubAckTimeout extends Event

  sealed abstract class Command
  case object ForwardUnsubscribe extends Command
  case object ForwardUnsubAck extends Command

  // State event handling

  def prepareServerUnpublisher(data: Start): Behavior[Event] = Behaviors.setup { context =>
    val reply = Promise[RemotePacketRouter.Registered.type]
    data.packetRouter ! RemotePacketRouter.Register(context.self.unsafeUpcast, data.clientId, data.packetId, reply)
    reply.future.onComplete {
      case Success(RemotePacketRouter.Registered) => context.self ! RegisteredPacketId
      case Failure(_)                             => context.self ! UnobtainablePacketId
    }(context.executionContext)

    Behaviors.receiveMessagePartial[Event] {
      case RegisteredPacketId =>
        data.local.success(ForwardUnsubscribe)
        serverUnsubscribe(
          ServerUnsubscribe(data.clientId, data.packetId, data.unsubscribed, data.packetRouter, data.settings))
      case UnobtainablePacketId =>
        data.local.failure(UnsubscribeFailed)
        data.unsubscribed.failure(UnsubscribeFailed)
        throw UnsubscribeFailed
    }
  }

  def serverUnsubscribe(data: ServerUnsubscribe): Behavior[Event] = Behaviors.withTimers { timer =>
    val ReceiveUnsubAck = "server-receive-unsubAck"
    timer.startSingleTimer(ReceiveUnsubAck, ReceiveUnsubAckTimeout, data.settings.receiveUnsubAckTimeout)

    Behaviors.receiveMessagePartial[Event] {
      case UnsubAckReceivedLocally(remote) =>
        remote.success(ForwardUnsubAck)
        data.unsubscribed.success(Done)
        Behaviors.stopped
      case ReceiveUnsubAckTimeout =>
        data.unsubscribed.failure(UnsubscribeFailed)
        throw UnsubscribeFailed
    }
  }
}
