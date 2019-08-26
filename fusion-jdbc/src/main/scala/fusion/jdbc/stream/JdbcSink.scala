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

package fusion.jdbc.stream

import java.sql.PreparedStatement

import akka.stream.scaladsl.Sink
import fusion.jdbc.ConnectionPreparedStatementCreator
import fusion.jdbc.util.JdbcUtils
import javax.sql.DataSource

import scala.concurrent.Future

object JdbcSink {

  def apply(creator: ConnectionPreparedStatementCreator, args: Iterable[Any], batchSize: Int = 100)(
      implicit dataSource: DataSource): Sink[Iterable[Any], Future[JdbcSinkResult]] =
    apply(creator, (args, stmt) => JdbcUtils.setStatementParameters(stmt, args), batchSize)

  def apply[T](creator: ConnectionPreparedStatementCreator, action: (T, PreparedStatement) => Unit, batchSize: Int)(
      implicit dataSource: DataSource): Sink[T, Future[JdbcSinkResult]] =
    Sink.fromGraph(new JdbcSinkStage[T](dataSource, creator, action, batchSize))

}
