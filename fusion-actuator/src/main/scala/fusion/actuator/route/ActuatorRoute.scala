package fusion.actuator.route

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.server.Route

trait ActuatorRoute extends Directives {
  val system: ExtendedActorSystem
  val name: String
  def isTemplated: Boolean     = false
  final def aroundRoute: Route = pathPrefix(name) { route }
  def route: Route
}
