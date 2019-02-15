package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, StatusCodes}
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.http.server.AbstractRoute
import fusion.http.util.HttpUtils

class Routes extends AbstractRoute with StrictLogging {
  override def route: Route =
    pathGet("hello") {
      complete("hello")
    } ~
      pathPost("aaa") {
        complete("aaa")
      }
}

class HttpApplicationTest extends HttpApplicationTestSuite {

  test("hello") {
    val url = s"http://$serverHost:$serverPort/hello"
    val response = HttpUtils.singleRequest(HttpMethods.GET, url).futureValue
    println(response)
    response.status mustBe StatusCodes.OK
  }

  override def createRoute: Route = (new Routes).route

  override protected def createActorSystem(): ActorSystem = ActorSystem("test")
}
