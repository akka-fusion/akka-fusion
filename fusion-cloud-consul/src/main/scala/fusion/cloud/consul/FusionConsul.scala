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

package fusion.cloud.consul

import com.google.common.net.HostAndPort
import com.orbitz.consul.Consul
import com.orbitz.consul.model.agent.Registration
import com.typesafe.config.{ Config, ConfigFactory }
import com.typesafe.scalalogging.StrictLogging

import java.nio.charset.{ Charset, StandardCharsets }
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-01 18:50:02
 */
class FusionConsul private (val consul: Consul) extends AutoCloseable with StrictLogging {

  def getConfig(key: String, localConfig: Config = ConfigFactory.parseString("{}")): Config = {
    val text = getValueAsString(key).getOrElse(
      throw new IllegalArgumentException(s"The key of Consul is not exists that is [$key]."))
    val config = ConfigFactory.parseString(text)
    config.withFallback(localConfig).withFallback(ConfigFactory.load()).resolve()
  }

  def getValueAsString(key: String, charset: Charset = StandardCharsets.UTF_8): Option[String] =
    consul.keyValueClient().getValueAsString(key, charset).toScala

  def getValuesAsString(key: String, charset: Charset = StandardCharsets.UTF_8): Vector[String] =
    consul.keyValueClient().getValuesAsString(key, charset).asScala.toVector

  def register(registration: Registration): FusionConsul = synchronized {
    logger.info(s"Register current service instance is $registration")
    consul.agentClient().register(registration)
    this
  }

  def deregister(serviceId: String): FusionConsul = synchronized {
    consul.agentClient().deregister(serviceId)
    this
  }

  override def close(): Unit = {
    consul.destroy()
    logger.info("Exit FusionConsul complete.")
  }
}

object FusionConsul {
  def apply(consul: Consul): FusionConsul = new FusionConsul(consul)

  def apply(host: String, port: Int): FusionConsul =
    apply(Consul.builder().withHostAndPort(HostAndPort.fromParts(host, port)).build())

  def fromByConfig(localConfig: Config = ConfigFactory.load()): FusionConsul =
    FusionConsul(localConfig.getString("fusion.cloud.consul.host"), localConfig.getInt("fusion.cloud.consul.port"))
}
