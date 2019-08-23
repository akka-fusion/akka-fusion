package fusion.discovery.client

import akka.Done
import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import helloscala.common.Configuration

import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

private[discovery] class FusionDiscoveryHttpClientComponents(system: ExtendedActorSystem)
    extends Components[DiscoveryHttpClient]("fusion.discovery.http-client") {
  import system.dispatcher

  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def createComponent(id: String): DiscoveryHttpClient = {
    val c = configuration.getConfiguration(id)
    c.get[Option[String]]("class") match {
      case Some(className) =>
        system.dynamicAccess
          .createInstanceFor[DiscoveryHttpClient](className, List(classOf[ExtendedActorSystem] -> system))
          .orElse(system.dynamicAccess
            .createInstanceFor[DiscoveryHttpClient](className, List(classOf[ActorSystem] -> system))) match {
          case Success(value) => value
          case Failure(e)     => throw new ExceptionInInitializerError(e)
        }
      case _ => DiscoveryHttpClient(new DiscoveryHttpClientSetting(c))(system)
    }
  }

  override protected def componentClose(c: DiscoveryHttpClient): Future[Done] = Future {
    c.close()
    Done
  }
}

class FusionDiscoveryHttpClient private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  def component: DiscoveryHttpClient = components.component
  val components                     = new FusionDiscoveryHttpClientComponents(_system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionHttpClient") { () =>
    components.closeAsync()(system.dispatcher)
  }
}

object FusionDiscoveryHttpClient extends ExtensionId[FusionDiscoveryHttpClient] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionDiscoveryHttpClient =
    new FusionDiscoveryHttpClient(system)
  override def lookup(): ExtensionId[_ <: Extension] = FusionDiscoveryHttpClient
}
