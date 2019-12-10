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

package fusion.common

import akka.actor.typed.receptionist.{ Receptionist, ServiceKey }
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem, Behavior, Props }
import akka.util.Timeout
import helloscala.common.exception.HSInternalErrorException

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ Await, Future }

trait FusionSystemCompanionTrait {
  def system: ActorSystem[FusionProtocol.Command]

  def spawnActor[REF](behavior: Behavior[REF], name: String)(implicit timeout: Timeout): Future[ActorRef[REF]] =
    spawnActor(behavior, name, Props.empty)

  def spawnActorSync[REF](behavior: Behavior[REF], name: String, duration: FiniteDuration): ActorRef[REF] = {
    implicit val timeout = Timeout(duration)
    Await.result(spawnActor(behavior, name), duration)
  }

  def spawnActorSync[REF](
      behavior: Behavior[REF],
      name: String,
      props: Props,
      duration: FiniteDuration): ActorRef[REF] = {
    implicit val timeout = Timeout(duration)
    Await.result(spawnActor(behavior, name, props), duration)
  }

  def spawnActor[REF](behavior: Behavior[REF], name: String, props: Props)(
      implicit timeout: Timeout): Future[ActorRef[REF]] = {
    implicit val scheduler = system.scheduler
    system.ask(FusionProtocol.Spawn(behavior, name, props))
  }

  def receptionistFind[T](serviceKey: ServiceKey[T], timeout: FiniteDuration)(
      func: Receptionist.Listing => ActorRef[T]): ActorRef[T] = {
    implicit val ec = system.executionContext
    implicit val scheduler = system.scheduler
    implicit val t: Timeout = timeout
    val f = system.receptionist
      .ask[Receptionist.Listing] { replyTo =>
        Receptionist.Find(serviceKey, replyTo)
      }
      //      .map { case ConfigManager.ConfigManagerServiceKey.Listing(refs) => refs.head }
      .map(func)
    Await.result(f, timeout)
  }

  def receptionistFindSet[T](serviceKey: ServiceKey[T], timeout: FiniteDuration): Set[ActorRef[T]] = {
    implicit val ec = system.executionContext
    implicit val t: Timeout = timeout
    implicit val scheduler = system.scheduler
    val f = system.receptionist.ask[Receptionist.Listing](Receptionist.Find(serviceKey)).map { listing =>
      if (listing.isForKey(serviceKey)) {
        listing.serviceInstances(serviceKey)
      } else {
        Set[ActorRef[T]]()
      }
    }
    Await.result(f, timeout)
  }

  def receptionistFindOne[T](serviceKey: ServiceKey[T], timeout: FiniteDuration): ActorRef[T] = {
    receptionistFindSet(serviceKey, timeout).headOption
      .getOrElse(throw HSInternalErrorException(s"$serviceKey not found!"))
  }
}
