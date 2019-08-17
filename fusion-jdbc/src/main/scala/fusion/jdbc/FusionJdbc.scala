package fusion.jdbc

import akka.Done
import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.zaxxer.hikari.HikariDataSource
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.jdbc.constant.JdbcConstants
import fusion.jdbc.util.JdbcUtils
import helloscala.common.Configuration

import scala.concurrent.Future

final private[jdbc] class JdbcComponents(system: ActorSystem)
    extends Components[HikariDataSource](JdbcConstants.PATH_DEFAULT) {
  import system.dispatcher
  override def configuration: Configuration = FusionCore(system).configuration

  override protected def componentClose(c: HikariDataSource): Future[Done] = Future {
    c.close()
    Done
  }
  override protected def createComponent(id: String): HikariDataSource =
    JdbcUtils.createHikariDataSource(configuration.getConfig(id))
}

class FusionJdbc private (val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new JdbcComponents(system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionJdbc") { () =>
    components.closeAsync()(system.dispatcher)
  }
  def component: HikariDataSource = components.component
}

object FusionJdbc extends ExtensionId[FusionJdbc] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionJdbc = new FusionJdbc(system)
  override def lookup(): ExtensionId[_ <: Extension]                    = FusionJdbc
}
