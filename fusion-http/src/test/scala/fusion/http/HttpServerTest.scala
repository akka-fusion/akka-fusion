package fusion.http

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import fusion.http.util.HttpUtils
import helloscala.common.exception.HSBadRequestException
import org.scalatest.BeforeAndAfterAll

class HttpServerTest extends TestKit(ActorSystem()) with FusionTestFunSuite with BeforeAndAfterAll {
  implicit private val mat = ActorMaterializer()
  private val route = path("hello") {
      complete("hello")
    } ~
        path("404") {
          throw HSBadRequestException("404 test")
          complete("404")
        }

  var binding: Http.ServerBinding = _

  test("bad") {
    val local = binding.localAddress
    println(local.getAddress.getHostAddress)
    println(local.getHostName + " " + local.getAddress + " " + local.getHostString + " " + local.getPort)
    val request  = HttpRequest(uri = s"http://${local.getHostString}:${local.getPort}/404")
    val response = HttpUtils.singleRequest(request).futureValue
    println(response)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    binding = new HttpServer("fusion.http.default", system.asInstanceOf[ExtendedActorSystem]).startRouteSync(route)
  }

  override protected def afterAll(): Unit = {
//    TimeUnit.MINUTES.sleep(5)
    super.afterAll()
  }
}
