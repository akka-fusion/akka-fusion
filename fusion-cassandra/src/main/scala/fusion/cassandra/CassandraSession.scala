/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
