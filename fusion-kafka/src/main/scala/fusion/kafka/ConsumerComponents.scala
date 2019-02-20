package fusion.kafka

import akka.actor.ActorSystem
import akka.kafka.ConsumerSettings
import fusion.core.util.Components
import helloscala.common.Configuration
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

class ConsumerComponents(system: ActorSystem) extends Components[ConsumerSettings[String, String]] {
  override val component: ConsumerSettings[String, String] = createConsumer(s"${KafkaConstants.PATH_ROOT}.consumer")

  override protected def lookupComponent(id: String): ConsumerSettings[String, String] =
    components.getOrElseUpdate(id, createConsumer(id))

  override protected def registerComponent(
      id: String,
      other: ConsumerSettings[String, String],
      replaceExists: Boolean
  ): ConsumerSettings[String, String] = {
    val conf = getConfiguration(id)
    val beReplace = conf.getOrElse[Boolean](id + ".replace-exists", replaceExists)
    components.get(id).foreach {
      case _ if beReplace =>
//        p.close()
        components.remove(id)
      case _ => throw new IllegalAccessException(s"KafkaProducer已存在，id: $id")
    }
    val consumer = createConsumer(id)
    components.put(id, consumer)
    consumer
  }

  override def close(): Unit = {
//    component.close()
//    for ((_, c) <- components) {
//      c.close()
//    }
  }

  def createConsumer(id: String): ConsumerSettings[String, String] = {
    val conf = getConfiguration(id)
    ConsumerSettings(conf.underlying, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(conf.getOrElse[String]("kafka-clients.bootstrap.servers", "localhost:9092"))
      .withGroupId(conf.getOrElse("kafka-clients.group.id", "default"))
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    conf.getOrElse[String]("kafka-clients." + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"))
  }

  def getConfiguration(id: String) =
    Configuration(
      system.settings.config.getConfig(id).withFallback(system.settings.config.getConfig(ConsumerSettings.configPath)))
}
