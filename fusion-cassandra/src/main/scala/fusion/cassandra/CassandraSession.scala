package fusion.cassandra

import java.lang

import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.api.core.context.DriverContext
import com.datastax.oss.driver.api.core.metadata.Metadata
import com.datastax.oss.driver.api.core.metrics.Metrics
import com.datastax.oss.driver.api.core.session.Request
import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession

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
