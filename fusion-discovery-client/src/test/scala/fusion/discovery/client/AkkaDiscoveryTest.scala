package fusion.discovery.client

import akka.actor.ActorSystem
import akka.discovery.Discovery
import akka.discovery.Lookup
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite

import scala.concurrent.Await
import scala.concurrent.duration._

class AkkaDiscoveryTest extends TestKit(ActorSystem()) with FusionTestFunSuite {
  test("discovery") {
    val discovery = Discovery(system).discovery
    val resolvedF = discovery.lookup(Lookup("service1"), 10.seconds)
    val resolved  = Await.result(resolvedF, 10.seconds)
    println(resolved)
  }
}
