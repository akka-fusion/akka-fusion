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

import akka.stream.Attributes
import akka.stream.Outlet
import akka.stream.SourceShape
import akka.stream.stage.GraphStage
import akka.stream.stage.GraphStageLogic
import akka.stream.stage.OutHandler
import fusion.jdbc.ConnectionPreparedStatementCreator
import fusion.jdbc.util.JdbcUtils
import javax.sql.DataSource

import scala.util.control.NonFatal

class JdbcSourceStage(dataSource: DataSource, creator: ConnectionPreparedStatementCreator, fetchRowSize: Int)
    extends GraphStage[SourceShape[ResultSet]] {

  private val out: Outlet[ResultSet] = Outlet("JdbcSource.out")

  override def shape: SourceShape[ResultSet] = SourceShape(out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic =
    new GraphStageLogic(shape) with OutHandler {
      var maybeConn = Option.empty[(Connection, Boolean, PreparedStatement, ResultSet)]

      setHandler(out, this)

      override def onPull(): Unit =
        maybeConn match {
          case Some((_, _, _, rs)) if rs.next() =>
            push(out, rs)
          case Some(_) =>
            completeStage()
          case None =>
            () // doing nothing, waiting for in preStart() to be completed
        }

      override def preStart(): Unit =
        try {
          val conn = dataSource.getConnection
          val autoCommit = conn.getAutoCommit
          conn.setAutoCommit(false)
          val stmt = creator(conn)
          val rs = stmt.executeQuery()
          //          rs.setFetchDirection(ResultSet.TYPE_FORWARD_ONLY)
          rs.setFetchSize(fetchRowSize)
          maybeConn = Option((conn, autoCommit, stmt, rs))
        } catch {
          case NonFatal(e) => failStage(e)
        }

      override def postStop(): Unit =
        maybeConn.foreach {
          case (conn, autoCommit, stmt, rs) =>
            JdbcUtils.closeResultSet(rs)
            JdbcUtils.closeStatement(stmt)
            conn.setAutoCommit(autoCommit)
            JdbcUtils.closeConnection(conn)
        }
    }

}
