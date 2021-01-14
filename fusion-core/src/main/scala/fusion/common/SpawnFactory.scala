/*
 * Copyright 2019-2021 helloscala.com
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

import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props}
import akka.util.Timeout
import helloscala.common.exception.HSInternalErrorException

import scala.concurrent.Await
import scala.concurrent.duration.FiniteDuration

trait SpawnFactory {
  def spawn[T](behavior: Behavior[T]): ActorRef[T] = spawn(behavior, Props.empty)

  def spawn[T](behavior: Behavior[T], name: String): ActorRef[T] = spawn(behavior, name, Props.empty)

  def spawn[T](behavior: Behavior[T], props: Props): ActorRef[T]

  def spawn[T](behavior: Behavior[T], name: String, props: Props): ActorRef[T]
}

trait ReceptionistFactory {
  def typedSystem: ActorSystem[Nothing]

  def receptionistFind[T](serviceKey: ServiceKey[T], timeout: FiniteDuration)(
      func: Receptionist.Listing => ActorRef[T]
  ): ActorRef[T] = {
    implicit val ec = typedSystem.executionContext
    implicit val t: Timeout = timeout
    implicit val scheduler = typedSystem.scheduler
    val f = typedSystem.receptionist
      .ask[Receptionist.Listing] { replyTo =>
        Receptionist.Find(serviceKey, replyTo)
      }
      //      .map { case ConfigManager.ConfigManagerServiceKey.Listing(refs) => refs.head }
      .map(func)
    Await.result(f, timeout)
  }

  def receptionistFindSet[T](serviceKey: ServiceKey[T])(implicit timeout: Timeout): Set[ActorRef[T]] = {
    implicit val ec = typedSystem.executionContext
    implicit val scheduler = typedSystem.scheduler
    val f = typedSystem.receptionist.ask[Receptionist.Listing](Receptionist.Find(serviceKey)).map { listing =>
      if (listing.isForKey(serviceKey)) {
        listing.serviceInstances(serviceKey)
      } else {
        Set[ActorRef[T]]()
      }
    }
    Await.result(f, timeout.duration)
  }

  def receptionistFindOne[T](serviceKey: ServiceKey[T])(implicit timeout: Timeout): ActorRef[T] = {
    receptionistFindSet(serviceKey).headOption.getOrElse(throw HSInternalErrorException(s"$serviceKey not found!"))
  }
}
