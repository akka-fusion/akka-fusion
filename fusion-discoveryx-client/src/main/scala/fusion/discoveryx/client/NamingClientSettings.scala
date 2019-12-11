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

package fusion.discoveryx.client

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import fusion.discoveryx.common.Constants

import scala.concurrent.duration.FiniteDuration
import scala.jdk.DurationConverters._

object NamingClientSettings {
  def apply(system: ActorSystem[_]): NamingClientSettings = fromConfig(system.settings.config)
  def fromConfig(config: Config): NamingClientSettings = new NamingClientSettings(config)
}
final class NamingClientSettings private (config: Config) {
  private val c = config.getConfig(s"${Constants.DISCOVERYX}.client.naming")
  val heartbeatInterval: FiniteDuration = c.getDuration("heartbeat-interval").toScala

  override def toString: String = c.root().toString
}
