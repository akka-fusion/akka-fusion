package fusion.discovery.client

import akka.Done
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.stream.ActorMaterializer
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import helloscala.common.Configuration

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

private[discovery] class FusionHttpClientComponents(system: ExtendedActorSystem)
    extends Components[DiscoveryHttpClient]("fusion.discovery.http-client.default") {
  import system.dispatcher

  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def createComponent(id: String): DiscoveryHttpClient = {
    val c = configuration.getConfiguration(id)
    val tryValue = system.dynamicAccess.createInstanceFor[DiscoveryHttpClient](
      c.getString("instance"),
      List(
        classOf[FusionNamingService] -> FusionHttpClient(system).component,
        classOf[ActorMaterializer]   -> ActorMaterializer()(system),
        classOf[Configuration]       -> c))
    tryValue match {
      case Success(httpClient) => httpClient
      case Failure(e)          => throw new ExceptionInInitializerError(e)
    }
  }

  override protected def componentClose(c: DiscoveryHttpClient): Future[Done] = Future {
    c.close()
    Done
  }
}

class FusionHttpClient private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  def component: DiscoveryHttpClient = components.component
  val components                     = new FusionHttpClientComponents(_system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionHttpClient") { () =>
    components.closeAsync()(system.dispatcher)
  }
}

object FusionHttpClient extends ExtensionId[FusionHttpClient] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionHttpClient = new FusionHttpClient(system)
  override def lookup(): ExtensionId[_ <: Extension]                          = FusionHttpClient
}
