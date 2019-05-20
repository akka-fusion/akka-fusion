package fusion.discovery.client.nacos

import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration

class ConfigurationTest extends FusionTestFunSuite {

  test("configuration") {
    val configuration = Configuration.fromDiscovery()
    println(configuration.toString)
    println(configuration.getString("fusion.name"))
  }

}
