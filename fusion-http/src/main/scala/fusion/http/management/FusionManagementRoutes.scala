package fusion.http.management

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.management.scaladsl.{ManagementRouteProvider, ManagementRouteProviderSettings}
import fusion.http.constant.HttpConstants
import fusion.http.util.HttpUtils
import helloscala.common.{Configuration, ErrCodes}

import scala.concurrent.Await
import scala.concurrent.duration._

class FusionManagementRoutes(system: ExtendedActorSystem) extends ManagementRouteProvider {
  override def routes(settings: ManagementRouteProviderSettings): Route = pathPrefix("fusion") {
    shutdownRoute ~
      healthRoute
  }

  def shutdownRoute: Route = (path("shutdown") & post) {
    val d = 1.second
    val msg = s"${d}后开始关闭Fusion系统"
    new Thread(() => {
      Thread.sleep(d.toMillis)
      system.terminate()
      val atMost =
        Configuration(system.settings.config).get[Duration](s"${HttpConstants.PATH_MANAGE}.terminate-timeout")
      Await.ready(system.whenTerminated, atMost)
    }).start()
    complete(HttpUtils.entityJson(s"""{"status":${ErrCodes.OK},"message":"$msg"}"""))
  }

  def healthRoute: Route = (path("health") & get) {
    complete(StatusCodes.OK)
  }

}
