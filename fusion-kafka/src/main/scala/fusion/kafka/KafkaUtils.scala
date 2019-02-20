package fusion.kafka

import helloscala.common.jackson.Jackson
import org.apache.kafka.clients.producer.ProducerRecord

object KafkaUtils {

  def stringProduceRecord(topic: String, value: Any) =
    new ProducerRecord[String, String](topic, Jackson.stringify(value))
}
