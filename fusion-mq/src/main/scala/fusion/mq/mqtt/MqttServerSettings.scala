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

package fusion.mq.mqtt

import akka.stream.alpakka.mqtt.streaming.MqttSessionSettings
import fusion.mq.MqServerSettings
import helloscala.common.Configuration

case class MqttServerSettings(sessionSettings: MqttSessionSettings, host: String, port: Int, maxConnections: Int)

object MqttServerSettings {
  def apply(
      mqttServerSettings: MqttServerSettings,
      configuration: Configuration,
      prefix: String = "fusion.mq.mqtt.server"): MqttServerSettings =
    apply(mqttServerSettings, configuration.getConfiguration(prefix))

  def apply(mqServerSettings: MqServerSettings, c: Configuration): MqttServerSettings = {
    MqttServerSettings(
      MqttSessionSettings(),
      c.getOrElse("host", mqServerSettings.host),
      c.getOrElse("port", mqServerSettings.port),
      c.getOrElse("max-connections", mqServerSettings.maxConnections))
  }
}
