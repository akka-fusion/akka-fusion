package fusion.http

import java.util.concurrent.TimeUnit

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
  implicit def ec = system.dispatcher
  implicit lazy val httpQueue = HttpUtils.cachedHostConnectionPool(s"http://$managementHost:$managementPort")

  test("hello") {
    val response = HttpUtils.singleRequest(HttpMethods.GET, s"http://$serverHost:$serverPort/hello").futureValue
    response.status mustBe StatusCodes.OK
  }

  test("/_management/ready") {
    val response = HttpUtils.hostRequest(HttpMethods.GET, "/_management/ready").futureValue
    response.status mustBe StatusCodes.OK
  }

  test("/_management/alive") {
    val response = HttpUtils.hostRequest(HttpMethods.GET, "/_management/alive").futureValue
    response.status mustBe StatusCodes.OK
  }

  override def createRoute: Route = (new Routes).route

  override protected def createActorSystem(): ActorSystem = ActorSystem("test")

  override def afterAll(): Unit = {
    super.afterAll()
//    TimeUnit.MINUTES.sleep(10)
  }

}
