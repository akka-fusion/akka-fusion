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

package fusion.mq.mqtt

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.stream.alpakka.mqtt.streaming.MqttCodec.DecodeError
import akka.stream.alpakka.mqtt.streaming._
import akka.stream.alpakka.mqtt.streaming.scaladsl.{ ActorMqttServerSession, Mqtt }
import akka.stream.scaladsl.{ BroadcastHub, Keep, Sink, Source, SourceQueueWithComplete, Tcp }
import akka.stream.{ KillSwitch, KillSwitches, OverflowStrategy }
import akka.util.ByteString
import fusion.mq.FusionMQ

import scala.concurrent.Future

class MqttServer(system: ActorSystem[_]) extends Runnable with AutoCloseable {
  private implicit val classicSystem = system.toClassic
  private var _binding: Option[Future[Tcp.ServerBinding]] = None
  private var _killSwitch: Option[KillSwitch] = None
  private val fusionMQ = FusionMQ(system)
  val serverSettings = MqttServerSettings(fusionMQ.settings.server, fusionMQ.configuration)
  val session = ActorMqttServerSession(serverSettings.sessionSettings)

  override def run(): Unit = {
    val bindSource = createBindSource()
    val (bound, killSwitch) = bindSource.viaMat(KillSwitches.single)(Keep.both).to(Sink.ignore).run()
    _binding = Some(bound)
    _killSwitch = Some(killSwitch)
  }

  private def createBindSource(): Source[Either[MqttCodec.DecodeError, Event[Nothing]], Future[Tcp.ServerBinding]] = {
    Tcp()
      .bind(serverSettings.host, serverSettings.port)
      .flatMapMerge(
        serverSettings.maxConnections, { connection =>
          val mqttFlow = Mqtt
            .serverSessionFlow(session, ByteString(connection.remoteAddress.getAddress.getAddress))
            .join(connection.flow) // midi out1(session flow command) ~> flow write, flow read ~> midi in2(session flow event)

          // (发送消息到客户端queue, 接收客户端消息事件)
          val (toClientQueue, fromClientSource) = Source
            .queue[Command[Nothing]](3, OverflowStrategy.dropHead)
            .via(mqttFlow)
            .toMat(BroadcastHub.sink)(Keep.both)
            .run()

          fromClientSource.runForeach(runEventForeach(toClientQueue, connection))
          fromClientSource
        })
  }

  private def runEventForeach(
      toClientQueue: SourceQueueWithComplete[Command[Nothing]],
      connection: Tcp.IncomingConnection): Either[DecodeError, Event[_]] => Unit = {
    case Right(Event(_: Connect, _)) =>
      toClientQueue.offer(Command(ConnAck(ConnAckFlags.None, ConnAckReturnCode.ConnectionAccepted)))
    case Right(Event(cp: Subscribe, _)) =>
      toClientQueue.offer(Command(SubAck(cp.packetId, cp.topicFilters.map(_._2)), None, None))
    case Right(Event(Publish(flags, _, Some(packetId), _), _)) if flags.contains(ControlPacketFlags.RETAIN) =>
      toClientQueue.offer(Command(PubAck(packetId)))
    case Right(Event(Unsubscribe(packetId, topicFilters), carry)) =>
      toClientQueue.offer(Command(UnsubAck(packetId)))
    case _ =>
  }

  def binding: Option[Future[Tcp.ServerBinding]] = _binding

  override def close(): Unit = {
    _killSwitch.foreach(_.shutdown())
    session.shutdown()
  }
}

object MqttServer {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem(Behaviors.ignore, "mqtt")
    new MqttServer(system).run()
  }
}
