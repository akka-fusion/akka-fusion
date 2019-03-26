package fusion.kafka

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.kafka.ProducerSettings

final class FusionKafkaProducer private (system: ExtendedActorSystem) extends Extension {
  def producer: ProducerSettings[String, String] = producers.component
  val producers                                  = new ProducerComponents(system)
}

object FusionKafkaProducer extends ExtensionId[FusionKafkaProducer] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionKafkaProducer = new FusionKafkaProducer(system)
  override def lookup(): ExtensionId[_ <: Extension]                             = FusionKafkaProducer
}
