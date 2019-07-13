package fusion.discovery.client

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import akka.stream.ActorMaterializer
import com.typesafe.config.Config
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import helloscala.common.Configuration

import scala.util.Failure
import scala.util.Success

private[discovery] class FusionHttpClientComponents(system: ExtendedActorSystem)
    extends Components[DiscoveryHttpClient]("fusion.discovery.http-client.default") {
  override def config: Config = system.settings.config

  override protected def createComponent(id: String): DiscoveryHttpClient = {
    val c = Configuration(config.getConfig(id))
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

  override protected def componentClose(c: DiscoveryHttpClient): Unit = c.close()
}

class FusionHttpClient private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  def component: DiscoveryHttpClient = components.component
  val components                     = new FusionHttpClientComponents(_system)
}

object FusionHttpClient extends ExtensionId[FusionHttpClient] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionHttpClient = new FusionHttpClient(system)
  override def lookup(): ExtensionId[_ <: Extension]                          = FusionHttpClient
}
