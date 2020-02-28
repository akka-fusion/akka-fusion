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
import akka.actor.typed._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.model.HttpHeader
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.{ FusionConstants, FusionKeys }
import fusion.common.extension.{ FusionCoordinatedShutdown, FusionExtension, FusionExtensionId }
import fusion.common.{ ReceptionistFactory, SpawnFactory }
import fusion.core.event.FusionEvents
import fusion.core.http.headers.`X-Service`
import fusion.core.setting.CoreSetting
import helloscala.common.util.{ PidFile, Utils }

import scala.util.control.NonFatal

final class FusionCore private (override val classicSystem: ExtendedActorSystem)
    extends FusionExtension
    with SpawnFactory
    with ReceptionistFactory
    with StrictLogging {
  val name: String = classicSystem.name
  val setting: CoreSetting = new CoreSetting(configuration)
  val events = new FusionEvents()
  val shutdowns = new FusionCoordinatedShutdown(classicSystem)
  val currentXService: HttpHeader = {
    val serviceName = configuration.get[Option[String]]("fusion.discovery.nacos.serviceName").getOrElse(name)
    `X-Service`(serviceName)
  }

  writePidfile()
  System.setProperty(
    FusionKeys.FUSION_NAME,
    if (classicSystem.settings.config.hasPath(FusionKeys.FUSION_NAME))
      classicSystem.settings.config.getString(FusionKeys.FUSION_NAME)
    else FusionConstants.FUSION)

  logger.info("FusionCore instanced!")

  override def spawn[T](behavior: Behavior[T], props: Props): ActorRef[T] =
    classicSystem.spawnAnonymous(behavior, props)

  override def spawn[T](behavior: Behavior[T], name: String, props: Props): ActorRef[T] =
    classicSystem.spawn(behavior, name, props)

  private def writePidfile(): Unit = {
    val config = classicSystem.settings.config
    val maybePidfile =
      if (config.hasPath(FusionKeys.PIDFILE)) Utils.option(config.getString(FusionKeys.PIDFILE)) else None

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
        logger.info(s"-D${FusionKeys.PIDFILE} 未设置，将不写入 .pid 文件。")
    }
  }
}

object FusionCore extends FusionExtensionId[FusionCore] {
  override def createExtension(system: ExtendedActorSystem): FusionCore = new FusionCore(system)
}
