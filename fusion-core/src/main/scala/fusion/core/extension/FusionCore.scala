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

package fusion.core.extension

import akka.actor.ExtendedActorSystem
import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.ServiceKey
import akka.http.scaladsl.model.HttpHeader
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import fusion.core.FusionProtocol
import fusion.core.event.FusionEvents
import fusion.core.extension.impl.FusionCoreImpl
import fusion.core.setting.CoreSetting
import helloscala.common.Configuration
import helloscala.common.exception.HSInternalErrorException

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

abstract class FusionCore extends Extension with StrictLogging {
  def name: String
  val setting: CoreSetting
  val events: FusionEvents
  val shutdowns: FusionCoordinatedShutdown
  val currentXService: HttpHeader
  val fusionGuardian: ActorRef[FusionProtocol.Command]
  def configuration: Configuration
  def fusionSystem: ActorSystem[FusionProtocol.Command]
  def classicSystem: ExtendedActorSystem

  def spawnActor[REF](behavior: Behavior[REF], name: String, props: Props)(
      implicit timeout: Timeout): Future[ActorRef[REF]]

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

  def receptionistFind[T](serviceKey: ServiceKey[T], timeout: FiniteDuration)(
      func: Receptionist.Listing => ActorRef[T]): ActorRef[T]

  def receptionistFindSet[T](serviceKey: ServiceKey[T], timeout: FiniteDuration): Set[ActorRef[T]]

  def receptionistFindOne[T](serviceKey: ServiceKey[T], timeout: FiniteDuration): ActorRef[T] = {
    receptionistFindSet(serviceKey, timeout).headOption
      .getOrElse(throw HSInternalErrorException(s"$serviceKey not found!"))
  }
}

object FusionCore extends ExtensionId[FusionCore] {
  override def createExtension(system: ActorSystem[_]): FusionCore = new FusionCoreImpl(system)
  def get(system: ActorSystem[_]): FusionCore = apply(system)
}
