package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import fusion.http.server.{AbstractRoute, AkkaHttpServer}
import helloscala.common.Configuration

import scala.concurrent.Future

class HttpApplication extends AkkaHttpServer {
  override def actorSystem: ActorSystem = ???
  override def actorMaterializer: ActorMaterializer = ???
  override val hlServerValue: String = actorSystem.name
  override def configuration: Configuration = ???
  override def routes: AbstractRoute = ???

  /**
   * 启动基于Akka HTTP的服务
   *
   * @return (http绑定，https绑定)
   */
  override def startServer(): (Future[Http.ServerBinding], Option[Future[Http.ServerBinding]]) =
    startServer("fusion.")
}
