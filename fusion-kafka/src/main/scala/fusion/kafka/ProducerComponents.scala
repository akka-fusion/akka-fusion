package fusion.kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import com.typesafe.config.Config
import fusion.core.util.Components
import helloscala.common.Configuration
import org.apache.kafka.common.serialization.StringSerializer

final private[kafka] class ProducerComponents(system: ActorSystem)
    extends Components[ProducerSettings[String, String]](s"${KafkaConstants.PATH_ROOT}.producer") {
  override def config: Config = system.settings.config

  override protected def createComponent(id: String): ProducerSettings[String, String] = {
    val conf = getConfiguration(id)
    ProducerSettings(conf.underlying, new StringSerializer, new StringSerializer)
      .withBootstrapServers(conf.getOrElse[String]("kafka-clients.bootstrap.servers", "localhost:9092"))
  }

  override protected def componentClose(c: ProducerSettings[String, String]): Unit = {}

  private def getConfiguration(id: String) =
    Configuration(
      system.settings.config.getConfig(id).withFallback(system.settings.config.getConfig(ProducerSettings.configPath)))
}
