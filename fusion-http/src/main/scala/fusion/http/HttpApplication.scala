package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import fusion.http.server.{AbstractRoute, AkkaHttpServer}
import helloscala.common.Configuration

import scala.concurrent.Future

class HttpApplication private (val system: ActorSystem, val routes: AbstractRoute) extends AkkaHttpServer {
  override val materializer: ActorMaterializer = ActorMaterializer()(system)
  override val hlServerValue: String = system.name
  private val _configuration = Configuration(system.settings.config)
  override def configuration: Configuration = _configuration

  /**
   * 启动基于Akka HTTP的服务
   *
   * @return (http绑定，https绑定)
   */
  override def startServer(): (Future[Http.ServerBinding], Option[Future[Http.ServerBinding]]) =
    startServer("fusion.")
}

object HttpApplication {

  def apply(system: ActorSystem, routes: AbstractRoute): HttpApplication = new HttpApplication(system, routes)

}
