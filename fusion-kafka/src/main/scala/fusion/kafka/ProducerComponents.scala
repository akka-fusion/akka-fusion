package fusion.kafka

import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import fusion.core.util.Components
import helloscala.common.Configuration
import org.apache.kafka.common.serialization.StringSerializer

class ProducerComponents(system: ActorSystem) extends Components[ProducerSettings[String, String]] {
  override val component: ProducerSettings[String, String] = createProducer(s"${KafkaConstants.PATH_ROOT}.producer")

  override protected def lookupComponent(id: String): ProducerSettings[String, String] =
    components.getOrElseUpdate(id, createProducer(id))

  override protected def registerComponent(
      id: String,
      other: ProducerSettings[String, String],
      replaceExists: Boolean
  ): ProducerSettings[String, String] = {
    val conf = getConfiguration(id)
    val beReplace = conf.getOrElse[Boolean](id + ".replace-exists", replaceExists)
    components.get(id).foreach {
      case _ if beReplace =>
//        p.close()
        components.remove(id)
      case _ => throw new IllegalAccessException(s"ProducerSettings已存在，id: $id")
    }
    val producer = createProducer(id)
    components.put(id, producer)
    producer
  }

  def createProducer(id: String): ProducerSettings[String, String] = {
    val conf = getConfiguration(id)
    ProducerSettings(conf.underlying, new StringSerializer, new StringSerializer)
      .withBootstrapServers(conf.getOrElse[String]("kafka-clients.bootstrap.servers", "localhost:9092"))
  }

  override def close(): Unit = {
//    component.close()
//    for ((_, c) <- components) {
//      c.close()
//    }
  }

  def getConfiguration(id: String) =
    Configuration(
      system.settings.config.getConfig(id).withFallback(system.settings.config.getConfig(ProducerSettings.configPath)))
}
