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

package akka.stream.alpakka.mqtt.streaming.scaladsl

import java.util.concurrent.atomic.AtomicLong

import akka.actor.ExtendedActorSystem
import akka.actor.typed.Props
import akka.actor.typed.internal.adapter.{ ActorRefAdapter, PropsAdapter }
import akka.actor.typed.scaladsl.adapter._
import akka.event.Logging
import akka.stream._
import akka.stream.alpakka.mqtt.streaming._
import akka.stream.alpakka.mqtt.streaming.impl._
import akka.stream.scaladsl.{ BroadcastHub, Flow, Keep, Source }
import akka.util.ByteString
import akka.{ Done, NotUsed, actor => untyped }

import scala.concurrent.{ Future, Promise }
import scala.util.control.NoStackTrace
import scala.util.{ Failure, Success }

object MqttServerSession {
  /**
   * Used to signal that a client session has ended
   */
  final case class ClientSessionTerminated(clientId: String)
}

/**
 * Represents server-only sessions
 */
abstract class MqttServerSession extends MqttSession {
  import MqttServerSession._
  import MqttSession._

  /**
   * Used to observe client connections being terminated
   */
  def watchClientSessions: Source[ClientSessionTerminated, NotUsed]

  /**
   * @return a flow for commands to be sent to the session in relation to a connection id
   */
  private[streaming] def commandFlow[A](connectionId: ByteString): CommandFlow[A]

  /**
   * @return a flow for events to be emitted by the session in relation t a connection id
   */
  private[streaming] def eventFlow[A](connectionId: ByteString): EventFlow[A]
}

object ActorMqttServerSession {
  def apply(
      settings: MqttSessionSettings)(implicit mat: Materializer, system: untyped.ActorSystem): ActorMqttServerSession =
    new ActorMqttServerSession(settings)

  /**
   * A PINGREQ was not received within the required keep alive period - the connection must close
   *
   * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/os/mqtt-v3.1.1-os.html
   * 3.1.2.10 Keep Alive
   */
  case object PingFailed extends Exception with NoStackTrace

  private[scaladsl] val serverSessionCounter = new AtomicLong
}

/**
 * Provides an actor implementation of a server session
 * @param settings session settings
 */
final class ActorMqttServerSession(settings: MqttSessionSettings)(
    implicit mat: Materializer,
    system: untyped.ActorSystem)
    extends MqttServerSession {
  import ActorMqttServerSession._
  import MqttServerSession._

  private val serverSessionId = serverSessionCounter.getAndIncrement()

  private val (terminations, terminationsSource) = Source
    .queue[ServerConnector.ClientSessionTerminated](
      settings.clientTerminationWatcherBufferSize,
      OverflowStrategy.backpressure)
    .toMat(BroadcastHub.sink)(Keep.both)
    .run()

  def watchClientSessions: Source[ClientSessionTerminated, NotUsed] =
    terminationsSource.map {
      case ServerConnector.ClientSessionTerminated(clientId) => ClientSessionTerminated(clientId)
    }

  private val consumerPacketRouter =
    ActorRefAdapter(
      system
        .asInstanceOf[ExtendedActorSystem]
        .systemActorOf(
          PropsAdapter(() => RemotePacketRouter[Consumer.Event], Props.empty, false),
          "server-consumer-packet-id-allocator-" + serverSessionId))
  private val producerPacketRouter =
    ActorRefAdapter(
      system
        .asInstanceOf[ExtendedActorSystem]
        .systemActorOf(
          PropsAdapter(() => LocalPacketRouter[Producer.Event], Props.empty, false),
          "server-producer-packet-id-allocator-" + serverSessionId))
  private val publisherPacketRouter =
    ActorRefAdapter(
      system
        .asInstanceOf[ExtendedActorSystem]
        .systemActorOf(
          PropsAdapter(() => RemotePacketRouter[Publisher.Event], Props.empty, false),
          "server-publisher-packet-id-allocator-" + serverSessionId))
  private val unpublisherPacketRouter =
    ActorRefAdapter(
      system
        .asInstanceOf[ExtendedActorSystem]
        .systemActorOf(
          PropsAdapter(() => RemotePacketRouter[Unpublisher.Event], Props.empty, false),
          "server-unpublisher-packet-id-allocator-" + serverSessionId))
  private val serverConnector =
    ActorRefAdapter(
      system
        .asInstanceOf[ExtendedActorSystem]
        .systemActorOf(
          PropsAdapter(
            () =>
              ServerConnector(
                terminations,
                consumerPacketRouter,
                producerPacketRouter,
                publisherPacketRouter,
                unpublisherPacketRouter,
                settings),
            Props.empty,
            false),
          "server-connector-" + serverSessionId))

  import MqttCodec._
  import MqttSession._
  import system.dispatcher

  override def ![A](cp: Command[A]): Unit = cp match {
    case Command(cp: Publish, _, carry) =>
      serverConnector ! ServerConnector.PublishReceivedLocally(cp, carry)
    case c: Command[A] => throw new IllegalStateException(s"$c is not a server command that can be sent directly")
  }

  override def shutdown(): Unit = {
    system.stop(serverConnector.toClassic)
    system.stop(consumerPacketRouter.toClassic)
    system.stop(producerPacketRouter.toClassic)
    system.stop(publisherPacketRouter.toClassic)
    system.stop(unpublisherPacketRouter.toClassic)
    terminations.complete()
  }

  private val pingRespBytes = PingResp.encode(ByteString.newBuilder).result()

  override def commandFlow[A](connectionId: ByteString): CommandFlow[A] =
    Flow
      .lazyFutureFlow { () =>
        val killSwitch = KillSwitches.shared("command-kill-switch-" + serverSessionId)

        Future.successful(
          Flow[Command[A]]
            .watch(serverConnector.toClassic)
            .watchTermination() {
              case (_, terminated) =>
                terminated.onComplete {
                  case Failure(_: WatchedActorTerminatedException) =>
                  case _ =>
                    serverConnector ! ServerConnector.ConnectionLost(connectionId)
                }
                NotUsed
            }
            .via(killSwitch.flow)
            .flatMapMerge(
              settings.commandParallelism, {
                case Command(cp: ConnAck, _, _) =>
                  val reply = Promise[Source[ClientConnection.ForwardConnAckCommand, NotUsed]]
                  serverConnector ! ServerConnector.ConnAckReceivedLocally(connectionId, cp, reply)
                  Source.futureSource(reply.future.map(s =>
                    s.map {
                        case ClientConnection.ForwardConnAck =>
                          cp.encode(ByteString.newBuilder).result()
                        case ClientConnection.ForwardPingResp =>
                          pingRespBytes
                        case ClientConnection.ForwardPublish(publish, packetId) =>
                          publish.encode(ByteString.newBuilder, packetId).result()
                        case ClientConnection.ForwardPubRel(packetId) =>
                          PubRel(packetId).encode(ByteString.newBuilder).result()
                      }
                      .mapError {
                        case ServerConnector.PingFailed => ActorMqttServerSession.PingFailed
                      }
                      .watchTermination() { (_, done) =>
                        done.onComplete {
                          case Success(_) => killSwitch.shutdown()
                          case Failure(t) => killSwitch.abort(t)
                        }
                      }))
                case Command(cp: SubAck, completed, _) =>
                  val reply = Promise[Publisher.ForwardSubAck.type]
                  publisherPacketRouter ! RemotePacketRouter
                    .RouteViaConnection(connectionId, cp.packetId, Publisher.SubAckReceivedLocally(reply), reply)

                  reply.future.onComplete { result =>
                    completed.foreach(_.complete(result.map(_ => Done)))
                  }

                  Source.future(reply.future.map(_ => cp.encode(ByteString.newBuilder).result())).recover {
                    case _: RemotePacketRouter.CannotRoute => ByteString.empty
                  }

                case Command(cp: UnsubAck, completed, _) =>
                  val reply = Promise[Unpublisher.ForwardUnsubAck.type]
                  unpublisherPacketRouter ! RemotePacketRouter
                    .RouteViaConnection(connectionId, cp.packetId, Unpublisher.UnsubAckReceivedLocally(reply), reply)

                  reply.future.onComplete { result =>
                    completed.foreach(_.complete(result.map(_ => Done)))
                  }

                  Source.future(reply.future.map(_ => cp.encode(ByteString.newBuilder).result())).recover {
                    case _: RemotePacketRouter.CannotRoute => ByteString.empty
                  }
                case Command(cp: PubAck, completed, _) =>
                  val reply = Promise[Consumer.ForwardPubAck.type]
                  consumerPacketRouter ! RemotePacketRouter
                    .RouteViaConnection(connectionId, cp.packetId, Consumer.PubAckReceivedLocally(reply), reply)

                  reply.future.onComplete { result =>
                    completed.foreach(_.complete(result.map(_ => Done)))
                  }

                  Source.future(reply.future.map(_ => cp.encode(ByteString.newBuilder).result())).recover {
                    case _: RemotePacketRouter.CannotRoute => ByteString.empty
                  }
                case Command(cp: PubRec, completed, _) =>
                  val reply = Promise[Consumer.ForwardPubRec.type]
                  consumerPacketRouter ! RemotePacketRouter
                    .RouteViaConnection(connectionId, cp.packetId, Consumer.PubRecReceivedLocally(reply), reply)

                  reply.future.onComplete { result =>
                    completed.foreach(_.complete(result.map(_ => Done)))
                  }

                  Source.future(reply.future.map(_ => cp.encode(ByteString.newBuilder).result())).recover {
                    case _: RemotePacketRouter.CannotRoute => ByteString.empty
                  }
                case Command(cp: PubComp, completed, _) =>
                  val reply = Promise[Consumer.ForwardPubComp.type]
                  consumerPacketRouter ! RemotePacketRouter
                    .RouteViaConnection(connectionId, cp.packetId, Consumer.PubCompReceivedLocally(reply), reply)

                  reply.future.onComplete { result =>
                    completed.foreach(_.complete(result.map(_ => Done)))
                  }

                  Source.future(reply.future.map(_ => cp.encode(ByteString.newBuilder).result())).recover {
                    case _: RemotePacketRouter.CannotRoute => ByteString.empty
                  }
                case c: Command[A] => throw new IllegalStateException(s"$c is not a server command")
              })
            .recover {
              case _: WatchedActorTerminatedException => ByteString.empty
            }
            .filter(_.nonEmpty)
            .log("server-commandFlow", _.iterator.decodeControlPacket(settings.maxPacketSize)) // we decode here so we can see the generated packet id
            .withAttributes(ActorAttributes.logLevels(onFailure = Logging.DebugLevel)))
      }
      .mapMaterializedValue(_ => NotUsed)

  override def eventFlow[A](connectionId: ByteString): EventFlow[A] =
    Flow[ByteString]
      .watch(serverConnector.toClassic)
      .watchTermination() {
        case (_, terminated) =>
          terminated.onComplete {
            case Failure(_: WatchedActorTerminatedException) =>
            case _ =>
              serverConnector ! ServerConnector.ConnectionLost(connectionId)
          }
          NotUsed
      }
      .via(new MqttFrameStage(settings.maxPacketSize))
      .map(_.iterator.decodeControlPacket(settings.maxPacketSize))
      .log("server-events")
      .mapAsync[Either[MqttCodec.DecodeError, Event[A]]](settings.eventParallelism) {
        case Right(cp: Connect) =>
          val reply = Promise[ClientConnection.ForwardConnect.type]
          serverConnector ! ServerConnector.ConnectReceivedFromRemote(connectionId, cp, reply)
          reply.future.map(_ => Right(Event(cp)))
        case Right(cp: Subscribe) =>
          val reply = Promise[Publisher.ForwardSubscribe.type]
          serverConnector ! ServerConnector.SubscribeReceivedFromRemote(connectionId, cp, reply)
          reply.future.map(_ => Right(Event(cp)))
        case Right(cp: Unsubscribe) =>
          val reply = Promise[Unpublisher.ForwardUnsubscribe.type]
          serverConnector ! ServerConnector.UnsubscribeReceivedFromRemote(connectionId, cp, reply)
          reply.future.map(_ => Right(Event(cp)))
        case Right(cp: Publish) =>
          val reply = Promise[Consumer.ForwardPublish.type]
          serverConnector ! ServerConnector.PublishReceivedFromRemote(connectionId, cp, reply)
          reply.future.map(_ => Right(Event(cp)))
        case Right(cp: PubAck) =>
          val reply = Promise[Producer.ForwardPubAck]
          producerPacketRouter ! LocalPacketRouter.Route(cp.packetId, Producer.PubAckReceivedFromRemote(reply), reply)
          reply.future.map {
            case Producer.ForwardPubAck(carry: Option[A] @unchecked) => Right(Event(cp, carry))
          }
        case Right(cp: PubRec) =>
          val reply = Promise[Producer.ForwardPubRec]
          producerPacketRouter ! LocalPacketRouter.Route(cp.packetId, Producer.PubRecReceivedFromRemote(reply), reply)
          reply.future.map {
            case Producer.ForwardPubRec(carry: Option[A] @unchecked) => Right(Event(cp, carry))
          }
        case Right(cp: PubRel) =>
          val reply = Promise[Consumer.ForwardPubRel.type]
          consumerPacketRouter ! RemotePacketRouter.RouteViaConnection(
            connectionId,
            cp.packetId,
            Consumer.PubRelReceivedFromRemote(reply),
            reply)
          reply.future.map(_ => Right(Event(cp)))
        case Right(cp: PubComp) =>
          val reply = Promise[Producer.ForwardPubComp]
          producerPacketRouter ! LocalPacketRouter.Route(cp.packetId, Producer.PubCompReceivedFromRemote(reply), reply)
          reply.future.map {
            case Producer.ForwardPubComp(carry: Option[A] @unchecked) => Right(Event(cp, carry))
          }
        case Right(PingReq) =>
          val reply = Promise[ClientConnection.ForwardPingReq.type]
          serverConnector ! ServerConnector.PingReqReceivedFromRemote(connectionId, reply)
          reply.future.map(_ => Right(Event(PingReq)))
        case Right(Disconnect) =>
          val reply = Promise[ClientConnection.ForwardDisconnect.type]
          serverConnector ! ServerConnector.DisconnectReceivedFromRemote(connectionId, reply)
          reply.future.map(_ => Right(Event(Disconnect)))
        case Right(cp) => Future.failed(new IllegalStateException(s"$cp is not a server event"))
        case Left(de)  => Future.successful(Left(de))
      }
      .withAttributes(ActorAttributes.supervisionStrategy {
        // Benign exceptions
        case _: LocalPacketRouter.CannotRoute | _: RemotePacketRouter.CannotRoute =>
          Supervision.Resume
        case _ =>
          Supervision.Stop
      })
      .recoverWithRetries(-1, {
        case _: WatchedActorTerminatedException => Source.empty
      })
      .withAttributes(ActorAttributes.logLevels(onFailure = Logging.DebugLevel))
}
