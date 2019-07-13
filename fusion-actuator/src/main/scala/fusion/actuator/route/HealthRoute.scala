package fusion.actuator.route

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.core.model.Health
import fusion.core.model.HealthComponent
import helloscala.common.jackson.Jackson
import helloscala.common.util.Utils

import scala.collection.JavaConverters._

final class HealthRoute(val system: ExtendedActorSystem) extends ActuatorRoute with StrictLogging {
  override val name = "health"

  private val healths = system.settings.config
    .getStringList("fusion.actuator.health.components")
    .iterator()
    .asScala
    .flatMap { fqcn =>
      Utils.try2option(
        system.dynamicAccess.getObjectFor[HealthComponent](fqcn),
        e => logger.error(s"获取object失败，fqcn: $fqcn", e))
    }
    .map(comp => comp.name -> comp)
    .toMap

  def route: Route =
    pathEndOrSingleSlash {
      complete(renderJson(Health.up(healths.mapValues(_.health))))
    } ~
        pathPrefix(Segment) { comp =>
          pathEndOrSingleSlash {
            healths.get(comp) match {
              case Some(health) => complete(renderJson(health))
              case _            => complete(StatusCodes.NotFound)
            }
          } ~
            path(Segment) { instance =>
              val maybe = healths.get(comp) match {
                case Some(health: HealthComponent) => health.health.details.get(instance)
                case _                             => None
              }
              maybe match {
                case Some(v) => complete(renderJson(v))
                case _       => complete(StatusCodes.NotFound)
              }
            }
        }

  def renderJson(v: Any) =
    HttpEntity(ContentTypes.`application/json`, Jackson.stringify(v))

}
