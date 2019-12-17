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

package akka.stream.alpakka.mqtt.streaming
package scaladsl

import akka.NotUsed
import akka.stream.{ Attributes, BidiShape, Inlet, Outlet }
import akka.stream.scaladsl.BidiFlow
import akka.stream.stage.{ GraphStage, GraphStageLogic, InHandler, OutHandler }
import akka.util.ByteString

object Mqtt {
  /**
   * Create a bidirectional flow that maintains client session state with an MQTT endpoint.
   * The bidirectional flow can be joined with an endpoint flow that receives
   * [[ByteString]] payloads and independently produces [[ByteString]] payloads e.g.
   * an MQTT server.
   *
   * @param session the MQTT client session to use
   * @param connectionId a identifier to distinguish the client connection so that the session
   *                     can route the incoming requests
   * @return the bidirectional flow
   */
  def clientSessionFlow[A](session: MqttClientSession, connectionId: ByteString)
      : BidiFlow[Command[A], ByteString, ByteString, Either[MqttCodec.DecodeError, Event[A]], NotUsed] =
    BidiFlow
      .fromFlows(session.commandFlow[A](connectionId), session.eventFlow[A](connectionId))
      .atop(BidiFlow.fromGraph(new CoupledTerminationBidi))

  /**
   * Create a bidirectional flow that maintains client session state with an MQTT endpoint.
   * The bidirectional flow can be joined with an endpoint flow that receives
   * [[ByteString]] payloads and independently produces [[ByteString]] payloads e.g.
   * an MQTT server.
   *
   * @param session the MQTT client session to use
   * @return the bidirectional flow
   */
  @deprecated("Provide a connectionId instead", "1.0.1")
  def clientSessionFlow[A](session: MqttClientSession)
      : BidiFlow[Command[A], ByteString, ByteString, Either[MqttCodec.DecodeError, Event[A]], NotUsed] =
    BidiFlow
      .fromFlows(session.commandFlow[A](ByteString("0")), session.eventFlow[A](ByteString("0")))
      .atop(BidiFlow.fromGraph(new CoupledTerminationBidi))

  /**
   * Create a bidirectional flow that maintains server session state with an MQTT endpoint.
   * The bidirectional flow can be joined with an endpoint flow that receives
   * [[ByteString]] payloads and independently produces [[ByteString]] payloads e.g.
   * an MQTT server.
   *
   * @param session the MQTT server session to use
   * @param connectionId a identifier to distinguish the client connection so that the session
   *                     can route the incoming requests
   * @return the bidirectional flow
   */
  def serverSessionFlow[A](session: MqttServerSession, connectionId: ByteString)
      : BidiFlow[Command[A], ByteString, ByteString, Either[MqttCodec.DecodeError, Event[A]], NotUsed] =
    BidiFlow
      .fromFlows(session.commandFlow[A](connectionId), session.eventFlow[A](connectionId))
      .atop(BidiFlow.fromGraph(new CoupledTerminationBidi))
}

/** INTERNAL API - taken from Akka streams - perhaps it should be made public */
private[scaladsl] class CoupledTerminationBidi[I, O] extends GraphStage[BidiShape[I, I, O, O]] {
  val in1: Inlet[I] = Inlet("CoupledCompletion.in1")
  val out1: Outlet[I] = Outlet("CoupledCompletion.out1")
  val in2: Inlet[O] = Inlet("CoupledCompletion.in2")
  val out2: Outlet[O] = Outlet("CoupledCompletion.out2")
  override val shape: BidiShape[I, I, O, O] = BidiShape(in1, out1, in2, out2)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {
    val handler1: InHandler with OutHandler = new InHandler with OutHandler {
      override def onPush(): Unit = push(out1, grab(in1))
      override def onPull(): Unit = pull(in1)
    }

    val handler2: InHandler with OutHandler = new InHandler with OutHandler {
      override def onPush(): Unit = push(out2, grab(in2))
      override def onPull(): Unit = pull(in2)
    }

    setHandlers(in1, out1, handler1)
    setHandlers(in2, out2, handler2)
  }
}
