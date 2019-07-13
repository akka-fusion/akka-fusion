package fusion.http

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.typesafe.config.Config
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.http.constant.HttpConstants

private[http] class HttpServerComponents(system: ExtendedActorSystem)
    extends Components[HttpServer](HttpConstants.PATH_DEFAULT) {
  override val config: Config                                    = system.settings.config
  override protected def createComponent(id: String): HttpServer = new HttpServer(id, system)
  override protected def componentClose(c: HttpServer): Unit     = c.close()
}

class FusionHttpServer private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val core: FusionCore      = FusionCore(system)
  val components            = new HttpServerComponents(_system)
  def component: HttpServer = components.component
}

object FusionHttpServer extends ExtensionId[FusionHttpServer] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionHttpServer = new FusionHttpServer(system)
  override def lookup(): ExtensionId[_ <: Extension]                          = FusionHttpServer
}
