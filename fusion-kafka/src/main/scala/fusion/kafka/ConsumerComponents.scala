package fusion.kafka

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import com.typesafe.config.Config
import fusion.core.util.Components
import helloscala.common.Configuration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

final private[kafka] class ConsumerComponents(system: ActorSystem)
    extends Components[ConsumerSettings[String, String]](s"${KafkaConstants.PATH_ROOT}.consumer") {
  override def config: Config = system.settings.config

  override protected def createComponent(id: String): ConsumerSettings[String, String] = {
    val conf = getConfiguration(id)
    ConsumerSettings(conf.underlying, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(conf.getOrElse[String]("kafka-clients.bootstrap.servers", "localhost:9092"))
      .withGroupId(conf.getOrElse("kafka-clients.group.id", "default"))
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    conf.getOrElse[String]("kafka-clients." + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"))
  }

  override protected def componentClose(c: ConsumerSettings[String, String]): Unit = {}

  private def getConfiguration(id: String) =
    Configuration(
      system.settings.config.getConfig(id).withFallback(system.settings.config.getConfig(ConsumerSettings.configPath)))
}
