package fusion.cassandra

import java.lang
import java.util.function.Supplier

import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider
import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.api.core.context.DriverContext
import com.datastax.oss.driver.api.core.metadata.Metadata
import com.datastax.oss.driver.api.core.metrics.Metrics
import com.datastax.oss.driver.api.core.session.Request
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionExtension
import fusion.core.util.Components
import helloscala.common.Configuration

import scala.compat.java8.FutureConverters._
import scala.compat.java8.OptionConverters._
import scala.concurrent.Future

final class CassandraSession(val session: CqlSession) extends FusionCassandraSession with AutoCloseable {
  def getName: String = session.getName

  def getMetadata: Metadata = session.getMetadata

  def isSchemaMetadataEnabled: Boolean = session.isSchemaMetadataEnabled

  def setSchemaMetadataEnabled(newValue: lang.Boolean): Future[Metadata] =
    session.setSchemaMetadataEnabled(newValue).toScala

  def refreshSchemaAsync(): Future[Metadata] = session.refreshSchemaAsync().toScala

  def checkSchemaAgreementAsync(): Future[lang.Boolean] = session.checkSchemaAgreementAsync().toScala

  def getContext: DriverContext = session.getContext

  def getKeyspace: Option[CqlIdentifier] = session.getKeyspace.asScala

  def getMetrics: Option[Metrics] = session.getMetrics.asScala

  def execute[RequestT <: Request, ResultT](request: RequestT, resultType: GenericType[ResultT]): ResultT =
    session.execute(request, resultType)

  def closeFuture(): Future[Void] = session.closeFuture().toScala

  def closeAsync(): Future[Void] = session.closeAsync().toScala

  def forceCloseAsync(): Future[Void] = session.forceCloseAsync().toScala

  override def close(): Unit = session.close()
}

class FusionComponents(system: ExtendedActorSystem)
    extends Components[CassandraSession]("fusion.data.cassandra.default")
    with StrictLogging {
  override def config: Configuration = Configuration(system.settings.config)

  override protected def createComponent(id: String): CassandraSession = {
    val c = loadConfig(id)
    val configLoader = new DefaultDriverConfigLoader(new Supplier[Config] {
      override def get(): Config = c
    })

    val builder = CqlSession.builder()

//    if (c.hasPath("username") && c.hasPath("password")) {
//      builder.withAuthProvider(new PlainTextAuthProvider(c.getString("username"), c.getString("password")))
//    }

    if (c.hasPath("keyspace")) {
      builder.withKeyspace(c.getString("keyspace"))
    }

    val session = builder.withConfigLoader(configLoader).build()

//    val codecRegistry = session.getConfiguration.getCodecRegistry
//    if (c.hasPath("instance-codecs")) {
//      c.get[Seq[String]]("instance-codecs").foreach { className =>
//        system.dynamicAccess.getClassFor[TypeCodec[_]](className) match {
//          case Success(clazz) =>
//            val codec = clazz.getDeclaredField("instance").get(clazz).asInstanceOf[TypeCodec[_]]
//            codecRegistry.register(codec)
//          case Failure(e) =>
//            logger.warn(s"获取instance-codec实例错误，className: $className", e)
//        }
//      }
//    }

//    if (c.hasPath("json-jackson")) {
//      val fqcn = c.getString("json-jackson")
//      system.dynamicAccess.getObjectFor[JacksonFactory](fqcn) match {
//        case Success(jacksonFactory) =>
//          codecRegistry.register(new JacksonJsonCodec(classOf[JsonNode], jacksonFactory.defaultObjectMapper))
//        case Failure(e) =>
//          logger.warn(s"构建 JacksonJsonCodec[JsonNode] 错误，需要 ${classOf[JacksonFactory].getName}，实际 $fqcn", e)
//      }
//    }

    new CassandraSession(session)
  }

  override protected def componentClose(c: CassandraSession): Unit = { c.close() }

  private def loadConfig(prefix: String): Config = { // Make sure we see the changes when reloading:
    val root = config.underlying
    // The driver's built-in defaults, under the default prefix in reference.conf:
    val reference = root.getConfig("datastax-java-driver")
    // Everything under your custom prefix in application.conf:
    val application = root.getConfig(prefix)
    application.withFallback(reference)
  }
}

class FusionCassandra private (protected val _system: ExtendedActorSystem) extends FusionExtension {
  val components = new FusionComponents(_system)
  system.registerOnTermination { components.close() }
  def component: CassandraSession = components.component
}

object FusionCassandra extends ExtensionId[FusionCassandra] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem): FusionCassandra = new FusionCassandra(system)
  override def lookup(): ExtensionId[_ <: Extension]                         = FusionCassandra
}
