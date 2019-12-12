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

package fusion.discoveryx.server.naming

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import fusion.discoveryx.common.Constants
import helloscala.common.Configuration

import scala.concurrent.duration.FiniteDuration

final class NamingSettings(configuration: Configuration) {
  private val c = configuration.getConfiguration(s"${Constants.DISCOVERYX}.server.naming")
  val enable: Boolean = c.getBoolean("enable")
  val heartbeatInterval: FiniteDuration = c.get[FiniteDuration]("heartbeat-interval")
  val defaultPage: Int = c.getInt("default-page")
  val defaultSize: Int = c.getInt("default-size")

  def findSize(size: Int): Int = if (size < defaultSize) defaultSize else size

  def findPage(page: Int): Int = if (page < defaultPage) defaultPage else page
}

object NamingSettings {
  def apply(system: ActorSystem[_]): NamingSettings = apply(system.settings.config)
  def apply(config: Config): NamingSettings = new NamingSettings(Configuration(config))
}
