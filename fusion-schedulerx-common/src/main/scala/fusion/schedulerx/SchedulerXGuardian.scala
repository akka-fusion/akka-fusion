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

package fusion.schedulerx

import akka.actor.typed.{ ActorRef, Behavior, Props }
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }

object SchedulerXGuardian {
  sealed trait Command
  case class Spawn[T](behavior: Behavior[T], name: String, props: Props, replyTo: ActorRef[ActorRef[T]]) extends Command
  case class SpawnAnonymous[T](behavior: Behavior[T], props: Props, replyTo: ActorRef[ActorRef[T]]) extends Command

  def apply(): Behavior[Command] = Behaviors.setup(context => new SchedulerXGuardian(context).init())
}

import SchedulerXGuardian._
class SchedulerXGuardian private (context: ActorContext[Command]) {
  def init(): Behavior[Command] = Behaviors.receiveMessage {
    case Spawn(behavior, name, props, replyTo) =>
      val child = context.spawn(behavior, name, props)
      replyTo ! child
      Behaviors.same
    case SpawnAnonymous(behavior, props, replyTo) =>
      val child = context.spawnAnonymous(behavior, props)
      replyTo ! child
      Behaviors.same
  }
}
