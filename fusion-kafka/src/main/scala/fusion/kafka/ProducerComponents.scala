package fusion.kafka

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import fusion.core.util.Components
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
