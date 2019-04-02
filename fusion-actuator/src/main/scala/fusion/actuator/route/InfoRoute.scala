package fusion.actuator.route

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging

final class InfoRoute(val system: ExtendedActorSystem) extends ActuatorRoute with StrictLogging {
  private val EMPTY         = HttpEntity(ContentTypes.`application/json`, "{}")
  override val name: String = "info"

  override def route: Route = get {
    complete(EMPTY)
  }

}
