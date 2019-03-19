package fusion.docs.sample

import helloscala.common.Configuration

object ConfigDemo extends App {
//
//  val str = """{
//              |  name = "fusion"
//              |  # Properties defined by org.apache.kafka.clients.producer.ProducerConfig
//              |  # can be defined in this configuration section.
//              |  kafka-clients {
//              |    group.id = "default"
//              |  }
//              |  akka.extensions = ["fusion.core.extensions.FusionCore"]
//              |  akka.extensions = ["fusion.core.extensions.DDD"]
//              |}""".stripMargin
//
//  val config = ConfigFactory.parseString(str)
//
//  val newConf = config.withFallback(ConfigFactory.load().getConfig("akka.kafka.producer"))
//
//  println(newConf)
//
//  config.getStringList("akka.extensions").forEach(println)

  sys.props.put(
    "config.nacos.url",
    "http://123.206.9.104:8849/nacos/v1/cs/configs?tenant=3cc379e7-d0c0-461c-9700-abe252c60151&dataId=hongka.resource.app&group=DEFAULT_GROUP"
  )
  val config = Configuration()
  println(config.underlying.root())
}
