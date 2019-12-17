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

package sample.stats

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import fusion.json.jackson.CborSerializable

import scala.concurrent.duration._

//#worker
object StatsWorker {
  trait Command
  final case class Process(word: String, replyTo: ActorRef[Processed]) extends Command with CborSerializable
  private case object EvictCache extends Command

  final case class Processed(word: String, length: Int) extends CborSerializable

  def apply(): Behavior[Command] = Behaviors.setup { ctx =>
    Behaviors.withTimers { timers =>
      ctx.log.info("Worker starting up")
      timers.startTimerWithFixedDelay(EvictCache, EvictCache, 30.seconds)

      withCache(ctx, Map.empty)
    }
  }

  private def withCache(ctx: ActorContext[Command], cache: Map[String, Int]): Behavior[Command] =
    Behaviors.receiveMessage {
      case Process(word, replyTo) =>
        ctx.log.info("Worker processing request")
        cache.get(word) match {
          case Some(length) =>
            replyTo ! Processed(word, length)
            Behaviors.same
          case None =>
            val length = word.length
            val updatedCache = cache + (word -> length)
            replyTo ! Processed(word, length)
            withCache(ctx, updatedCache)
        }
      case EvictCache =>
        withCache(ctx, Map.empty)
    }
}
//#worker
