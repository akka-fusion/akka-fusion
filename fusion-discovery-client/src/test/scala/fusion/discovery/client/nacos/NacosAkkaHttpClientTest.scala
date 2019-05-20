package fusion.discovery.client.nacos

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite
import org.scalatest.BeforeAndAfterAll

class NacosAkkaHttpClientTest extends TestKit(ActorSystem("test")) with FusionTestFunSuite with BeforeAndAfterAll {
  test("configService") {
    val configService = FusionNacos(system).component.configService
    val confStr       = configService.getConfig
    confStr must not be empty
    println(confStr)
  }

  test("namingService") {
    val namingService = FusionNacos(system).component.namingService
    TimeUnit.SECONDS.sleep(3)
    val instances = namingService.getAllInstances("hongka-file-app")
    instances must not be empty
    instances.foreach(println)
  }

}
