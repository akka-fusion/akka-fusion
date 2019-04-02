package fusion.http

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import fusion.core.extension.FusionExtension

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future

final class FusionHttp private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  private var _httpApplication: HttpApplication = _

  def httpApplication: HttpApplication = _httpApplication

  implicit def materializer: ActorMaterializer = httpApplication.materializer

  def startAwait(route: Route): HttpApplication = {
    val (httpF, maybeHttpsF) = startAsync(route)
    Await.result(httpF, 60.seconds)
    _httpApplication
  }

  def startAsync(route: Route): (Future[Http.ServerBinding], Option[Future[Http.ServerBinding]]) = {
    _httpApplication = HttpApplication(system, route)
    httpApplication.startServer()
  }

}

object FusionHttp extends ExtensionId[FusionHttp] with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): FusionHttp = new FusionHttp(system)
  override def lookup(): ExtensionId[_ <: Extension]                    = FusionHttp
}
