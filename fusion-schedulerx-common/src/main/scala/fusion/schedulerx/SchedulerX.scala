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

import java.util.concurrent.atomic.AtomicLong

import akka.actor.Address
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.{ actor => classic }
import com.typesafe.config.Config
import fusion.common.FusionProtocol
import helloscala.common.util.DigestUtils

class SchedulerX private (val schedulerXSettings: SchedulerXSettings, val system: ActorSystem[FusionProtocol.Command]) {
  def classicSystem: classic.ActorSystem = system.toClassic
}

object SchedulerX {
  private val _counter = new AtomicLong(0)
  def counter(): Long = _counter.getAndIncrement()

  @inline def getWorkerId(address: Address): String = DigestUtils.sha1Hex(address.hostPort)

  def apply(originalConfig: Config): SchedulerX = apply(SchedulerXSettings(originalConfig))

  def apply(schedulerXSettings: SchedulerXSettings): SchedulerX = {
    new SchedulerX(
      schedulerXSettings,
      ActorSystem(SchedulerXGuardian(), schedulerXSettings.name, schedulerXSettings.config))
  }
}
