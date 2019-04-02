package fusion.actuator.route

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.actuator.setting.ActuatorSetting
import helloscala.common.jackson.Jackson
import helloscala.common.util.Utils

import scala.collection.JavaConverters._

case class Item(href: String, templated: Boolean)

class FusionActuatorRoute(system: ExtendedActorSystem, actuatorSetting: ActuatorSetting) extends StrictLogging {
  private val components = system.settings.config.getStringList("fusion.actuator.routes").asScala.flatMap { fqcn =>
    Utils.try2option(
      system.dynamicAccess.createInstanceFor[ActuatorRoute](fqcn, List(classOf[ExtendedActorSystem] -> system)),
      e => logger.error(s"创建实例失败，fqcn: $fqcn", e))
  }
  private val routes = components.map(_.aroundRoute)
  private def links(request: HttpRequest) =
    components.map { comp =>
      val href = request.uri
        .copy(path = Uri.Path(s"/${actuatorSetting.contextPath}/${comp.name}"), rawQueryString = None, fragment = None)
        .toString()
      comp.name -> Item(href, comp.isTemplated)
    }.toMap

  def route: Route =
    pathPrefix(actuatorSetting.contextPath) {
      pathEndOrSingleSlash {
        extractRequest { request =>
          complete {
            HttpEntity(ContentTypes.`application/json`, Jackson.stringify(Map("_links" -> links(request))))
          }
        }
      } ~
        concat(routes: _*)
    }
}
