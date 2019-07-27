package fusion.http.demo

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import com.typesafe.config.ConfigFactory
import fusion.http.FusionHttpServer
import fusion.http.util.HttpUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object FusionHttpServerDemo {

  def main(args: Array[String]): Unit = {
//    System.setProperty("fusion.http.default.server.host", "127.0.0.1")
    ConfigFactory.invalidateCaches()
    val system     = ActorSystem("http")
    val httpServer = FusionHttpServer(system).component
    httpServer.startRouteSync(createRoute())
    println(s"socket address is ${httpServer.socketAddress}")
    println(s"socket inet address is ${httpServer.socketAddress.getAddress}")
    TimeUnit.SECONDS.sleep(5)
    Await.result(system.terminate(), Duration.Inf)
  }

  def createRoute(): Route = {
    import akka.http.scaladsl.server.Directives._
    pathPrefix("api") {
      path("hello") {
        complete(HttpUtils.entityJson("""{"msg":"Hello"}"""))
      }
    }
  }
}
