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
import fusion.schedulerx.protocol.ServerStatus
import helloscala.common.config.FusionConfigFactory
import helloscala.common.util.DigestUtils
import oshi.SystemInfo

class SchedulerX private (
    val schedulerXSettings: SchedulerXSettings,
    val config: Config,
    val system: ActorSystem[FusionProtocol.Command]) {
  def classicSystem: classic.ActorSystem = system.toClassic
}

object SchedulerX {
  private val systemInfo = new SystemInfo()
  private val hal = systemInfo.getHardware
  private val os = systemInfo.getOperatingSystem
  private val _counter = new AtomicLong(0)
  def counter(): Long = _counter.getAndIncrement()

  @inline def getWorkerId(address: Address): String = DigestUtils.sha1Hex(address.hostPort)

  def fromOriginalConfig(originalConfig: Config): SchedulerX = {
    val config = FusionConfigFactory.arrangeConfig(originalConfig, Constants.SCHEDULERX, Seq("akka"))
    val settings = SchedulerXSettings(config)
    apply(settings, config)
  }

  def apply(schedulerXSettings: SchedulerXSettings, config: Config): SchedulerX = {
    new SchedulerX(schedulerXSettings, config, ActorSystem(SchedulerXGuardian(), schedulerXSettings.name, config))
  }

  def serverStatus(): ServerStatus = {
    val memory = hal.getMemory
    val fileSystem = os.getFileSystem
    var totalSpace = 0L
    var freeSpace = 0L
    for (fs <- fileSystem.getFileStores) {
      totalSpace += fs.getTotalSpace
      freeSpace += fs.getFreeSpace
    }
    val loadAverage = hal.getProcessor.getSystemLoadAverage(3)
    val runtime = Runtime.getRuntime
    ServerStatus(
      runtime.availableProcessors(),
      memory.getTotal,
      memory.getAvailable,
      totalSpace,
      freeSpace,
      loadAverage)
  }
}
