package fusion.docs.sample

import com.typesafe.config.ConfigFactory
import helloscala.common.Configuration

object ConfigDemo extends App {
//  sys.props.put(
//    "config.nacos.url",
//    "http://123.206.9.104:8849/nacos/v1/cs/configs?tenant=3cc379e7-d0c0-461c-9700-abe252c60151&dataId=hongka.resource.app&group=DEFAULT_GROUP"
//  )
//  val config = Configuration()
//  println(config.underlying.root())
  println(Configuration.instance().getString("fusion.name"))
  System.setProperty("fusion.name", "yangbajing")
  ConfigFactory.invalidateCaches()
  Configuration.instance(Configuration())
  println(Configuration.instance().getString("fusion.name"))
  println(Configuration.instance().getString("fusion.name"))
}
