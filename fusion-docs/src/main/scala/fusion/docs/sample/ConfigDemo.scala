package fusion.docs.sample

import com.typesafe.config.ConfigFactory

object ConfigDemo extends App {

  val str = """{
              |  name = "fusion"
              |  # Properties defined by org.apache.kafka.clients.producer.ProducerConfig
              |  # can be defined in this configuration section.
              |  kafka-clients {
              |    group.id = "default"
              |  }
              |  akka.extensions = ["fusion.core.extensions.FusionCore"]
              |  akka.extensions = ["fusion.core.extensions.DDD"]
              |}""".stripMargin

  val config = ConfigFactory.parseString(str)

  val newConf = config.withFallback(ConfigFactory.load().getConfig("akka.kafka.producer"))

  println(newConf)

  config.getStringList("akka.extensions").forEach(println)
}
