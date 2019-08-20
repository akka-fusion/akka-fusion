package fusion.http

import akka.Done
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.http.constant.HttpConstants
import helloscala.common.Configuration

import scala.concurrent.Future

private[http] class HttpServerComponents(system: ExtendedActorSystem)
    extends Components[HttpServer](HttpConstants.PATH_DEFAULT) {
  override val configuration: Configuration                          = Configuration(system.settings.config)
  override protected def createComponent(id: String): HttpServer     = new HttpServer(id, system)
  override protected def componentClose(c: HttpServer): Future[Done] = c.closeAsync()
}

class FusionHttpServer private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components            = new HttpServerComponents(_system)
  def component: HttpServer = components.component
  FusionCore(system).shutdowns.serviceUnbind("StopFusionHttpServer") { () =>
    components.closeAsync()(system.dispatcher)
  }
}

object FusionHttpServer extends ExtensionId[FusionHttpServer] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionHttpServer = new FusionHttpServer(system)
  override def lookup(): ExtensionId[_ <: Extension]                          = FusionHttpServer
}
