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

import java.nio.file.Paths

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.http.scaladsl.model.HttpHeader
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.ConfigKeys
import fusion.common.constant.FusionConstants
import fusion.core.event.FusionEvents
import fusion.core.http.headers.`X-Service`
import fusion.core.setting.CoreSetting
import fusion.core.util.FusionUtils
import helloscala.common.Configuration
import helloscala.common.util.PidFile
import helloscala.common.util.Utils

import scala.util.control.NonFatal

final class FusionCore private (protected val _system: ExtendedActorSystem) extends FusionExtension with StrictLogging {
  val name: String = system.name
  val setting: CoreSetting = new CoreSetting(configuration)
  val events = new FusionEvents()
  val shutdowns = new FusionCoordinatedShutdown(system)
  FusionUtils.setupActorSystem(system)
  writePidfile()
  System.setProperty(
    FusionConstants.NAME_PATH,
    if (system.settings.config.hasPath(FusionConstants.NAME_PATH))
      system.settings.config.getString(FusionConstants.NAME_PATH)
    else FusionConstants.NAME)

  private lazy val _configuration = new Configuration(system.settings.config)

  override def configuration: Configuration = _configuration

  val currentXService: HttpHeader = {
    val serviceName = configuration.get[Option[String]]("fusion.discovery.nacos.serviceName").getOrElse(name)
    `X-Service`(serviceName)
  }

  private def writePidfile(): Unit = {
    val config = system.settings.config
    val maybePidfile =
      if (config.hasPath(ConfigKeys.FUSION.PIDFILE)) Utils.option(config.getString(ConfigKeys.FUSION.PIDFILE)) else None

    maybePidfile match {
      case Some(pidfile) =>
        try {
          PidFile(Utils.getPid).create(Paths.get(pidfile), deleteOnExit = true)
        } catch {
          case NonFatal(e) =>
            logger.error(s"将进程ID写入文件：$pidfile 失败", e)
            System.exit(-1)
        }
      case _ =>
        logger.warn(s"-D${ConfigKeys.FUSION.PIDFILE} 未设置，将不写入 .pid 文件。")
    }

  }

}

object FusionCore extends ExtensionId[FusionCore] with ExtensionIdProvider {
  override def lookup(): ExtensionId[_ <: Extension] = FusionCore
  override def createExtension(system: ExtendedActorSystem): FusionCore = new FusionCore(system)
}
