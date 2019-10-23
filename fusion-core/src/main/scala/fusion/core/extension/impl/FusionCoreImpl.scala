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

package fusion.core.extension.impl

import java.nio.file.Paths

import akka.actor.ExtendedActorSystem
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Props
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.HttpHeader
import akka.util.Timeout
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.ConfigKeys
import fusion.common.constant.FusionConstants
import fusion.core.FusionProtocol
import fusion.core.event.FusionEvents
import fusion.core.extension.FusionCoordinatedShutdown
import fusion.core.extension.FusionCore
import fusion.core.http.headers.`X-Service`
import fusion.core.setting.CoreSetting
import fusion.core.util.FusionUtils
import helloscala.common.Configuration
import helloscala.common.util.PidFile
import helloscala.common.util.Utils

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.control.NonFatal
import akka.actor.typed.scaladsl.adapter._

private[fusion] class FusionCoreImpl(val system: ActorSystem[_]) extends FusionCore with StrictLogging {

  override def name: String = system.name
  override val setting: CoreSetting = new CoreSetting(configuration)
  override val events = new FusionEvents()
  override val shutdowns = new FusionCoordinatedShutdown(classicSystem)
  FusionUtils.setupActorSystem(system)
  writePidfile()
  System.setProperty(
    FusionConstants.NAME_PATH,
    if (system.settings.config.hasPath(FusionConstants.NAME_PATH))
      system.settings.config.getString(FusionConstants.NAME_PATH)
    else FusionConstants.NAME)

  logger.info("FusionCore instanced!")

  private lazy val _configuration = new Configuration(system.settings.config)

  override def configuration: Configuration = _configuration

  override def fusionSystem: ActorSystem[FusionProtocol.Command] =
    system.asInstanceOf[ActorSystem[FusionProtocol.Command]]

  override def classicSystem: ExtendedActorSystem = system.toClassic match {
    case v: ExtendedActorSystem => v
    case _                      => throw new IllegalStateException("Need ExtendedActorSystem instance.")
  }

  val currentXService: HttpHeader = {
    val serviceName = configuration.get[Option[String]]("fusion.discovery.nacos.serviceName").getOrElse(name)
    `X-Service`(serviceName)
  }

  override def spawnUserActor[REF](behavior: Behavior[REF], name: String, props: Props): Future[ActorRef[REF]] = {
    implicit val timeout = Timeout(3.seconds)
    implicit val scheduler = system.scheduler

    fusionSystem.ask(FusionProtocol.Spawn(behavior, name, props))
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
