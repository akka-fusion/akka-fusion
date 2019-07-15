package fusion.jdbc

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

final private[jdbc] class JdbcComponents(val config: Configuration)
    extends Components[HikariDataSource](JdbcConstants.PATH_DEFAULT) {
  override protected def componentClose(c: HikariDataSource): Unit = c.close()
  override protected def createComponent(id: String): HikariDataSource =
    JdbcUtils.createHikariDataSource(config.getConfig(id))
}

class FusionJdbc private (val _system: ExtendedActorSystem) extends FusionExtension {
  FusionCore(system)
  val components = new JdbcComponents(Configuration(system.settings.config))
  system.registerOnTermination { components.close() }
  def component: HikariDataSource = components.component
}

object FusionJdbc extends ExtensionId[FusionJdbc] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionJdbc = {
    val v = new FusionJdbc(system)
    println(v._system)
    v
  }
  override def lookup(): ExtensionId[_ <: Extension] = FusionJdbc
}
