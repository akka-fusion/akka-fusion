package fusion.http

import akka.actor.ActorSystem
import fusion.core.inject.Injects
import fusion.http.server.AbstractRoute

trait FusionServer {
  val routes: AbstractRoute

  def start(): HttpApplication = {
    val system = Injects.instance[ActorSystem]
    FusionHttp(system).startAwait(routes.route)
  }

}
