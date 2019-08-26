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

package fusion.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import fusion.core.component.Components
import helloscala.common.Configuration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future

final private[kafka] class ConsumerComponents(system: ActorSystem)
    extends Components[ConsumerSettings[String, String]](s"${KafkaConstants.PATH_ROOT}.consumer") {
  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def createComponent(id: String): ConsumerSettings[String, String] = {
    val conf = getConfiguration(id)
    ConsumerSettings(conf.underlying, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(conf.getOrElse[String]("kafka-clients.bootstrap.servers", "localhost:9092"))
      .withGroupId(conf.getOrElse("kafka-clients.group.id", "default"))
      .withProperty(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        conf.getOrElse[String]("kafka-clients." + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"))
  }

  override protected def componentClose(c: ConsumerSettings[String, String]): Future[Done] = Future.successful(Done)

  private def getConfiguration(id: String) =
    Configuration(
      system.settings.config.getConfig(id).withFallback(system.settings.config.getConfig(ConsumerSettings.configPath)))
}
