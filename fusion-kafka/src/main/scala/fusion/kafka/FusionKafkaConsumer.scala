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

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.kafka.ConsumerSettings

final class FusionKafkaConsumer private (system: ExtendedActorSystem) extends Extension {
  def consumer: ConsumerSettings[String, String] = consumers.component
  val consumers = new ConsumerComponents(system)
}

object FusionKafkaConsumer extends ExtensionId[FusionKafkaConsumer] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionKafkaConsumer = new FusionKafkaConsumer(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionKafkaConsumer
}
