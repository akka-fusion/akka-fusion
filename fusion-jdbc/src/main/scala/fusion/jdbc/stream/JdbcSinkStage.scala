/*
 * Copyright 2019 akka-fusion.com
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

import akka.stream.Attributes
import akka.stream.Inlet
import akka.stream.SinkShape
import akka.stream.stage.GraphStageLogic
import akka.stream.stage.GraphStageWithMaterializedValue
import akka.stream.stage.InHandler
import fusion.jdbc.ConnectionPreparedStatementCreator
import fusion.jdbc.util.JdbcUtils
import javax.sql.DataSource

import scala.concurrent.Future
import scala.concurrent.Promise
import scala.util.control.NonFatal

class JdbcSinkStage[T](
    dataSource: DataSource,
    creator: ConnectionPreparedStatementCreator,
    actionBinder: (T, PreparedStatement) => Unit,
    batchSize: Int = 100)
    extends GraphStageWithMaterializedValue[SinkShape[T], Future[JdbcSinkResult]] {
  val in: Inlet[T] = Inlet("JdbcSink.in")

  override def shape: SinkShape[T] = SinkShape(in)

  override def createLogicAndMaterializedValue(
      inheritedAttributes: Attributes): (GraphStageLogic, Future[JdbcSinkResult]) = {
    val promise = Promise[JdbcSinkResult]()

    val logic = new GraphStageLogic(shape) with InHandler {
      var count = 0
      var results: JdbcSinkResult = JdbcSinkResult(0L, Vector())
      var maybeConn = Option.empty[(Connection, Boolean, PreparedStatement)]

      setHandler(in, this)

      override def onPush(): Unit =
        maybeConn match {
          case Some((_, _, stmt)) =>
            val v = grab(in)
            actionBinder(v, stmt)
            stmt.addBatch()

            count += 1
            if (count % batchSize == 0) {
              writeToDB()
            }
            pull(in)

          case None =>
            () // do nothing
        }

      override def onUpstreamFinish(): Unit = {
        writeToDB()
        promise.trySuccess(results)
        completeStage()
      }

      override def onUpstreamFailure(e: Throwable): Unit =
        setupFailure(e)

      override def preStart(): Unit =
        try {
          val conn = dataSource.getConnection
          val autoCommit = conn.getAutoCommit
          conn.setAutoCommit(false)
          val stmt = creator(conn)
          maybeConn = Option((conn, autoCommit, stmt))
          pull(in)
        } catch {
          case NonFatal(e) =>
            setupFailure(e)
        }

      override def postStop(): Unit =
        for {
          (conn, autoCommit, stmt) <- maybeConn
        } {
          JdbcUtils.closeStatement(stmt)
          conn.setAutoCommit(autoCommit)
          JdbcUtils.closeConnection(conn)
        }

      private def writeToDB(): Unit =
        for {
          (conn, _, stmt) <- maybeConn if count > 0
        } {
          try {
            val batchs = stmt.executeBatch().toVector
            conn.commit()
            results = results.copy(count = results.count + batchs.size, results = results.results :+ batchs)
          } catch {
            case NonFatal(e) =>
              conn.rollback()
              setupFailure(e)
          }
        }

      private def setupFailure(e: Throwable): Unit = {
        promise.tryFailure(e)
        failStage(e)
      }
    }

    (logic, promise.future)
  }
}
