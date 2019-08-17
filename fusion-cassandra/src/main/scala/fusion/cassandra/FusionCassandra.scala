package fusion.cassandra

import java.util.function.Supplier

import akka.Done
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import helloscala.common.Configuration

import scala.concurrent.Future

class FusionComponents(system: ExtendedActorSystem)
    extends Components[CassandraSession]("fusion.data.cassandra.default")
    with StrictLogging {
  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def createComponent(id: String): CassandraSession = {
    val c = loadConfig(id)
    val configLoader = new DefaultDriverConfigLoader(new Supplier[Config] {
      override def get(): Config = c
    })
    val builder = CqlSession.builder()
    if (c.hasPath("keyspace")) {
      builder.withKeyspace(c.getString("keyspace"))
    }
    val session = builder.withConfigLoader(configLoader).build()
    new CassandraSession(session)
  }

  override protected def componentClose(c: CassandraSession): Future[Done] = {
    import system.dispatcher
    c.closeAsync().map(_ => Done)
  }

  private def loadConfig(prefix: String): Config = { // Make sure we see the changes when reloading:
    val root = configuration.underlying
    // The driver's built-in defaults, under the default prefix in reference.conf:
    val reference = root.getConfig("datastax-java-driver")
    // Everything under your custom prefix in application.conf:
    val application = root.getConfig(prefix)
    application.withFallback(reference).resolve()
  }
}

class FusionCassandra private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new FusionComponents(_system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionCassandra") { () =>
    components.closeAsync()(system.dispatcher)
  }

  def component: CassandraSession = components.component
}

object FusionCassandra extends ExtensionId[FusionCassandra] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionCassandra = new FusionCassandra(system)
  override def lookup(): ExtensionId[_ <: Extension]                         = FusionCassandra
}
