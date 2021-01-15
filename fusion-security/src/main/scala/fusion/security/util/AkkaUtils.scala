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

package fusion.security.util

import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import fusion.security.exception.AkkaSecurityException

import scala.concurrent.Await
import scala.concurrent.Future

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 16:15:26
 */
object AkkaUtils {

  def spawn[T](behavior: Behavior[T], name: String, props: Props = Props.empty)(
      implicit
      creator: RecipientRef[SpawnProtocol.Command],
      timeout: Timeout,
      scheduler: Scheduler): Future[ActorRef[T]] = {
    creator.ask[ActorRef[T]](replyTo => SpawnProtocol.Spawn(behavior, name, props, replyTo))
  }

  def spawnBlock[T](behavior: Behavior[T], name: String, props: Props = Props.empty)(
      implicit
      creator: RecipientRef[SpawnProtocol.Command],
      timeout: Timeout,
      scheduler: Scheduler): ActorRef[T] = {
    Await.result(spawn(behavior, name, props), timeout.duration)
  }

  def receptionistFindSet[T](
      serviceKey: ServiceKey[T])(implicit system: ActorSystem[_], timeout: Timeout): Future[Set[ActorRef[T]]] = {
    implicit val ec = system.executionContext
    system.receptionist.ask[Receptionist.Listing](Receptionist.Find(serviceKey)).map { listing =>
      if (listing.isForKey(serviceKey)) listing.serviceInstances(serviceKey) else Set[ActorRef[T]]()
    }
  }

  def receptionistFindOne[T](
      serviceKey: ServiceKey[T])(implicit system: ActorSystem[_], timeout: Timeout): Future[ActorRef[T]] = {
    implicit val ec = system.executionContext
    receptionistFindSet(serviceKey).flatMap {
      case set if set.nonEmpty => Future.successful(set.head)
      case _                   => Future.failed(new AkkaSecurityException(s"$serviceKey not found!"))
    }
  }

  def receptionistFindSetSync[T](
      serviceKey: ServiceKey[T])(implicit system: ActorSystem[_], timeout: Timeout): Set[ActorRef[T]] = {
    Await.result(receptionistFindSet(serviceKey), timeout.duration)
  }

  def receptionistFindOneSync[T](
      serviceKey: ServiceKey[T])(implicit system: ActorSystem[_], timeout: Timeout): ActorRef[T] = {
    receptionistFindSetSync(serviceKey).headOption.getOrElse(throw new AkkaSecurityException(s"$serviceKey not found!"))
  }
}
