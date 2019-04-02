package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.management.scaladsl.AkkaManagement
import akka.stream.ActorMaterializer
import fusion.core.constant.ConfigKeys
import fusion.core.constant.FusionConstants
import fusion.http.server.AkkaHttpServer
import helloscala.common.Configuration

import scala.concurrent.Future

class HttpApplication private (val system: ActorSystem, _routes: Route) extends AkkaHttpServer {
  override val materializer: ActorMaterializer = ActorMaterializer()(system)
  override val hlServerValue: String           = system.name
  private val _configuration                   = Configuration(system.settings.config)
  override val routes: Route = {
    if (system.settings.config.getBoolean(ConfigKeys.AKKA_MANAGEMENT_FUSION_ENABLE)) {
      AkkaManagement(system).start(settings => {
        settings
      })
    }
    _routes
  }
  override def configuration: Configuration = _configuration

  /**
   * 启动基于Akka HTTP的服务
   *
   * @return (http绑定，https绑定)
   */
  override def startServer(): (Future[Http.ServerBinding], Option[Future[Http.ServerBinding]]) =
    startServer(FusionConstants.ROOT_PREFIX)
}

object HttpApplication {

  def apply(system: ActorSystem, routes: Route): HttpApplication = {
    new HttpApplication(system, routes)
  }

}
