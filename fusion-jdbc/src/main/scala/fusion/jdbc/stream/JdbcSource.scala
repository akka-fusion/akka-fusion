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

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

import akka.NotUsed
import akka.stream.scaladsl.Source
import fusion.jdbc.ConnectionPreparedStatementCreator
import fusion.jdbc.util.JdbcUtils
import javax.sql.DataSource

object JdbcSource {

  def apply(sql: String, args: Iterable[Any], fetchRowSize: Int)(
      implicit dataSource: DataSource): Source[ResultSet, NotUsed] = {
    val connCreator = new ConnectionPreparedStatementCreator {
      override def apply(conn: Connection): PreparedStatement = {
        val stmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        JdbcUtils.setStatementParameters(stmt, args)
      }
    }
    Source.fromGraph(new JdbcSourceStage(dataSource, connCreator, fetchRowSize))
  }

  def apply(creator: ConnectionPreparedStatementCreator, fetchRowSize: Int)(
      implicit dataSource: DataSource): Source[ResultSet, NotUsed] =
    Source.fromGraph(new JdbcSourceStage(dataSource, creator, fetchRowSize))

}
