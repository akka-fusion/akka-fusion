package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.Route
import fusion.http.server.AbstractRoute
import fusion.http.util.HttpUtils

class Routes extends AbstractRoute {
  override def route: Route = path("hello") {
    complete("hello")
  }
}

class HttpApplicationTest extends HttpApplicationTestSuite {

  test("hello") {
    val response = HttpUtils.singleRequest(HttpMethods.GET, "http://localhost:55555/hello").futureValue
    println(response)
    response.status mustBe StatusCodes.OK
  }

  override protected def createActorSystem(): ActorSystem = ActorSystem("test")
}
