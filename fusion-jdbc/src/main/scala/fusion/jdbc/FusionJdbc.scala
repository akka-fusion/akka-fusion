package fusion.jdbc

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import fusion.jdbc.constant.JdbcConstants
import fusion.jdbc.util.JdbcUtils

final private[jdbc] class JdbcComponents(val config: Config)
    extends Components[HikariDataSource](s"${JdbcConstants.PATH_ROOT}.default") {
  override protected def componentClose(c: HikariDataSource): Unit = c.close()

  override protected def createComponent(id: String): HikariDataSource =
    JdbcUtils.createHikariDataSource(config.getConfig(id))
}

class FusionJdbc private (val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new JdbcComponents(system.settings.config)
  system.registerOnTermination(components.close())
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
