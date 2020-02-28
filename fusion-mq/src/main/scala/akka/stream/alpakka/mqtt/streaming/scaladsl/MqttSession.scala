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

import akka.NotUsed
import akka.stream.alpakka.mqtt.streaming._
import akka.stream.scaladsl.Flow
import akka.util.ByteString

object MqttSession {
  private[streaming] type CommandFlow[A] =
    Flow[Command[A], ByteString, NotUsed]
  private[streaming] type EventFlow[A] =
    Flow[ByteString, Either[MqttCodec.DecodeError, Event[A]], NotUsed]
}

/**
 * Represents MQTT session state for both clients or servers. Session
 * state can survive across connections i.e. their lifetime is
 * generally longer.
 */
abstract class MqttSession {
  /**
   * Tell the session to perform a command regardless of the state it is
   * in. This is important for sending Publish messages in particular,
   * as a connection may not have been established with a session.
   * @param cp The command to perform
   * @tparam A The type of any carry for the command.
   */
  final def tell[A](cp: Command[A]): Unit =
    this ! cp

  /**
   * Tell the session to perform a command regardless of the state it is
   * in. This is important for sending Publish messages in particular,
   * as a connection may not have been established with a session.
   * @param cp The command to perform
   * @tparam A The type of any carry for the command.
   */
  def ![A](cp: Command[A]): Unit

  /**
   * Shutdown the session gracefully
   */
  def shutdown(): Unit
}
