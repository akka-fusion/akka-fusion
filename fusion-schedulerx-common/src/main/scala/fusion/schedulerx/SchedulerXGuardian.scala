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

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ ActorContext, Behaviors }
import fusion.common.FusionProtocol

object SchedulerXGuardian {
  def apply(): Behavior[FusionProtocol.Command] = Behaviors.setup(context => new SchedulerXGuardian(context).init())
}
class SchedulerXGuardian private (context: ActorContext[FusionProtocol.Command]) {
  def init(): Behavior[FusionProtocol.Command] = Behaviors.receiveMessage {
    case FusionProtocol.Spawn(behavior, name, props, replyTo) =>
      val child = context.spawn(behavior, name, props)
      replyTo ! child
      Behaviors.same
  }
}
