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

import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import akka.actor.typed.{ Behavior, Signal }

import scala.collection.immutable.Seq

object BehaviorRunner {
  sealed trait Interpretable[T]

  final case class StoredMessage[T](message: T) extends Interpretable[T]

  final case class StoredSignal[T](signal: Signal) extends Interpretable[T]

  /**
   * Interpreter all of the supplied messages or signals, returning
   * the resulting behavior.
   */
  def run[T](behavior: Behavior[T], context: ActorContext[T], stash: Seq[Interpretable[T]]): Behavior[T] =
    stash.foldLeft(Behavior.start(behavior, context)) {
      case (b, StoredMessage(msg)) =>
        val nextBehavior = Behavior.interpretMessage(b, context, msg)

        if ((nextBehavior ne Behaviors.same) && (nextBehavior ne Behaviors.unhandled)) {
          nextBehavior
        } else {
          b
        }

      case (b, StoredSignal(signal)) =>
        val nextBehavior = Behavior.interpretSignal(b, context, signal)

        if ((nextBehavior ne Behaviors.same) && (nextBehavior ne Behaviors.unhandled)) {
          nextBehavior
        } else {
          b
        }
    }
}
