package fusion.config.server

import akka.actor.ActorSystem
import fusion.config.server.controller.Routes
import fusion.http.FusionHttpServer

object FusionConfigServerApplication extends App {
  implicit val system = ActorSystem()
  val route           = new Routes().route
  FusionHttpServer(system).component.startRouteSync(route)
}
