package fusion.discovery.client

import akka.discovery.Discovery
import akka.testkit.TestKit
import fusion.core.util.FusionUtils
import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration

class DiscoveryHttpClientTest
    extends TestKit(FusionUtils.createActorSystem(Configuration.fromDiscovery()))
    with FusionTestFunSuite {
  test("http client") {
    val discovery = Discovery(system).discovery
    println(discovery)
  }

}
