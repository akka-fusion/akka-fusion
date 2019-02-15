package fusion.http

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import fusion.core.extension.FusionExtension

final class FusionHttp private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  private var _httpApplication: HttpApplication = _

  def httpApplication: HttpApplication = _httpApplication

  implicit def materializer: ActorMaterializer = httpApplication.materializer

  def startAwait(route: Route): HttpApplication = {
    _httpApplication = HttpApplication(system, route)
    httpApplication.startServerAwait()
    httpApplication
  }
}

object FusionHttp extends ExtensionId[FusionHttp] with ExtensionIdProvider {

  override def createExtension(system: ExtendedActorSystem): FusionHttp = new FusionHttp(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionHttp
}
