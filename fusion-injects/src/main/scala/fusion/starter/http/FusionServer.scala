package fusion.starter.http

import akka.actor.ActorSystem
import fusion.http.server.AbstractRoute
import fusion.http.FusionHttp
import fusion.http.HttpApplication
import fusion.inject.Injects

trait FusionServer {
  val routes: AbstractRoute

  def start(): HttpApplication = {
    val system = Injects.instance[ActorSystem]
    FusionHttp(system).startAwait(routes.route)
  }

}
