package fusion.http

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite
import akka.http.scaladsl.server.Directives._

class HttpServerTest extends TestKit(ActorSystem()) with FusionTestFunSuite {
  test("startRouteSync") {
    val route = path("hello") {
      complete("hello")
    }
    val binding = HttpServer("fusion.http.default", system.asInstanceOf[ExtendedActorSystem]).startRouteSync(route)
    println(binding)
  }

}
