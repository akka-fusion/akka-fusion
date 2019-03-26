package fusion.config.server

import akka.actor.ActorSystem
import fusion.config.server.controller.Routes
import fusion.http.FusionHttp

object FusionConfigServerApplication extends App {
  implicit val system = ActorSystem()
  val route           = new Routes().route
  FusionHttp(system).startAwait(route)
}
