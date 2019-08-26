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
import akka.kafka.ProducerSettings
import fusion.core.component.Components
import helloscala.common.Configuration
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.Future

final private[kafka] class ProducerComponents(system: ActorSystem)
    extends Components[ProducerSettings[String, String]](s"${KafkaConstants.PATH_ROOT}.producer") {
  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def createComponent(id: String): ProducerSettings[String, String] = {
    val conf =
      configuration.getConfiguration(id).withFallback(configuration.getConfiguration(ProducerSettings.configPath))
    ProducerSettings(conf.underlying, new StringSerializer, new StringSerializer)
      .withBootstrapServers(conf.getOrElse[String]("kafka-clients.bootstrap.servers", "localhost:9092"))
  }

  override protected def componentClose(c: ProducerSettings[String, String]): Future[Done] = Future.successful(Done)
}
