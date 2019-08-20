package fusion.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import fusion.http.util.HttpUtils
import fusion.test.FusionTestFunSuite
import org.scalatest.BeforeAndAfterAll

class HttpServerTest extends TestKit(ActorSystem()) with FusionTestFunSuite with BeforeAndAfterAll {
  implicit private val mat = ActorMaterializer()
  private val route = path("hello") {
      complete("hello")
    } ~
        path("404") {
          complete("404")
        }

  test("bad") {
    val local = FusionHttpServer(system).component.socketAddress
    println(local.getAddress.getHostAddress)
    println(local.getHostName + " " + local.getAddress + " " + local.getHostString + " " + local.getPort)
    val request =
      HttpRequest(
        uri = Uri(s"http://${local.getHostString}:${local.getPort}/404")
          .withQuery(Uri.Query("name" -> "羊八井", "age" -> 33.toString, "username" -> "yangbajing")))
    val response = HttpUtils.singleRequest(request).futureValue
    println(response)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FusionHttpServer(system).component.startRouteSync(route)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }

}
