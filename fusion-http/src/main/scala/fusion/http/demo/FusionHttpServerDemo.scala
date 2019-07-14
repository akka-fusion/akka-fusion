package fusion.http.demo

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import fusion.http.FusionHttpServer
import fusion.http.util.HttpUtils

object FusionHttpServerDemo {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("http")
    FusionHttpServer(system).component.startRouteSync(createRoute())
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
