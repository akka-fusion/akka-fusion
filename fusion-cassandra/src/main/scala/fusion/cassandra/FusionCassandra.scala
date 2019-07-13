package fusion.cassandra

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.datastax.driver.core.Cluster
import com.datastax.driver.core.PlainTextAuthProvider
import com.datastax.driver.core.Session
import com.datastax.driver.core.TypeCodec
import com.datastax.driver.extras.codecs.json.JacksonJsonCodec
import com.fasterxml.jackson.databind.JsonNode
import com.google.common.util.concurrent.ListenableFuture
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import helloscala.common.Configuration
import helloscala.common.jackson.JacksonFactory

import scala.util.Failure
import scala.util.Success

case class CassandraCluster(cluster: Cluster, keyspace: Option[String], configuration: Configuration)
    extends AutoCloseable {
  def connect(): Session                                        = cluster.connect()
  def connect(keyspace: String): Session                        = cluster.connect(keyspace)
  def connectAsync(): ListenableFuture[Session]                 = cluster.connectAsync()
  def connectAsync(keyspace: String): ListenableFuture[Session] = cluster.connectAsync(keyspace)

  override def close(): Unit = cluster.close()
}

class FusionComponents(system: ExtendedActorSystem)
    extends Components[CassandraCluster]("fusion.cassandra.default")
    with StrictLogging {
  private val DEFAULT_HOST    = "127.0.0.1"
  private val DEFAULT_PORT    = 9042
  override def config: Config = system.settings.config
  private val configuration   = Configuration(config)

  override protected def createComponent(id: String): CassandraCluster = {
    val c = configuration.getConfiguration(id)

    val builder = if (c.hasPath("contacts")) {
      Cluster.builder().addContactPoints(c.get[Seq[String]]("contacts"): _*)
    } else {
      Cluster.builder.addContactPoint(DEFAULT_HOST)
    }
    builder.withPort(c.getOrElse("port", DEFAULT_PORT))

    if (c.hasPath("username") && c.hasPath("password")) {
      builder.withAuthProvider(new PlainTextAuthProvider(c.getString("username"), c.getString("password")))
    }

    val cluster = builder.build()

    val codecRegistry = cluster.getConfiguration.getCodecRegistry
    if (c.hasPath("instance-codecs")) {
      c.get[Seq[String]]("instance-codecs").foreach { className =>
        system.dynamicAccess.getClassFor[TypeCodec[_]](className) match {
          case Success(clazz) =>
            val codec = clazz.getDeclaredField("instance").get(clazz).asInstanceOf[TypeCodec[_]]
            codecRegistry.register(codec)
          case Failure(e) =>
            logger.warn(s"获取instance-codec实例错误，className: $className", e)
        }
      }
    }

    if (c.hasPath("json-jackson")) {
      val fqcn = c.getString("json-jackson")
      system.dynamicAccess.getObjectFor[JacksonFactory](fqcn) match {
        case Success(jacksonFactory) =>
          codecRegistry.register(new JacksonJsonCodec(classOf[JsonNode], jacksonFactory.defaultObjectMapper))
        case Failure(e) =>
          logger.warn(s"构建 JacksonJsonCodec[JsonNode] 错误，需要 ${classOf[JacksonFactory].getName}，实际 $fqcn", e)
      }
    }

    val keyspace =
      if (c.hasPath("keyspace")) Option(c.getString("keyspace")) else None
    CassandraCluster(cluster, keyspace, c)
  }

  override protected def componentClose(c: CassandraCluster): Unit = { c.close() }
}

class FusionCassandra private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new FusionComponents(_system)
  system.registerOnTermination { components.close() }
  def component: CassandraCluster = components.component
}

object FusionCassandra extends ExtensionId[FusionCassandra] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionCassandra = new FusionCassandra(system)
  override def lookup(): ExtensionId[_ <: Extension]                         = FusionCassandra
}
