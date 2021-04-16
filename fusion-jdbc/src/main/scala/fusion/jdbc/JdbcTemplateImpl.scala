/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.jdbc

import com.typesafe.scalalogging.Logger
import com.zaxxer.hikari.HikariDataSource
import fusion.jdbc.util.JdbcUtils
import helloscala.common.util.Utils
import org.slf4j.LoggerFactory

import java.sql.{Connection, PreparedStatement, ResultSet}
import java.util.Objects
import scala.annotation.varargs
import scala.collection.mutable
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

private[jdbc] class JdbcTemplateImpl(
    val dataSource: HikariDataSource,
    _useTransaction: Boolean,
    ignoreWarnings: Boolean,
    _allowPrintLog: Boolean)
    extends JdbcTemplate {
  private[this] val logger = Logger(LoggerFactory.getLogger(getClass.getName))

  private[this] def needUseTransaction(
      implicit
      conn: Connection = JdbcTemplate.EmptyConnection)
      : Boolean = //    if (conn != JdbcTemplate.EmptyConnection) true else _useTransaction
    conn != JdbcTemplate.EmptyConnection || _useTransaction

  private[this] def allowPrintLog: Boolean = _allowPrintLog && logger.underlying.isDebugEnabled

  override def withTransaction[R](func: Connection => R): R = {
    val conn = dataSource.getConnection
    val isCommit = conn.getAutoCommit
    conn.setAutoCommit(false)
    try {
      val result = func(conn)
      conn.commit()
      result
    } catch {
      case NonFatal(e) =>
        conn.rollback()
        throw e
    } finally {
      if (conn != null) {
        conn.setAutoCommit(isCommit) // XXX 连接已被关闭，有必要重置吗？
        conn.close()
      }
    }
  }

  @varargs
  override def count(sql: String, args: Any*): Long = size(sql, args)

  override def namedSize(sql: String, args: Map[String, Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Long = {
    val (_sql, paramIndex) = JdbcUtils.namedParameterToQuestionMarked(sql)
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(_sql, sql),
      JdbcUtils.preparedStatementAction(args, new PreparedStatementAction[Long] {
        override def apply(pstmt: PreparedStatement): Long = {
          JdbcUtils.setStatementParameters(pstmt, args, paramIndex)
          val rs = pstmt.executeQuery()
          if (rs.next()) Utils.parseLong(rs.getObject(1), 0L) else 0L
        }
      }),
      needUseTransaction)
  }

  override def size(sql: String, args: Seq[Any])(implicit connection: Connection = JdbcTemplate.EmptyConnection): Long =
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(sql),
      JdbcUtils.preparedStatementAction(args, new PreparedStatementAction[Long] {

        override def apply(pstmt: PreparedStatement): Long = {
          JdbcUtils.setStatementParameters(pstmt, args)
          val rs = pstmt.executeQuery()
          if (rs.next()) Utils.parseLong(rs.getObject(1), 0L) else 0L
        }
      }),
      needUseTransaction)

  override def listForMap(sql: String, args: Seq[Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): List[Map[String, Object]] =
    listForObject(sql, args, JdbcUtils.resultSetToMap)

  override def listForObject[R](sql: String, args: Seq[Any], rowMapper: (ResultSet) => R)(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): List[R] =
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(sql),
      JdbcUtils.preparedStatementAction(args, new PreparedStatementAction[List[R]] {

        override def apply(stmt: PreparedStatement): List[R] = {
          JdbcUtils.setStatementParameters(stmt, args)
          val rs = stmt.executeQuery()
          val buffer = mutable.Buffer.empty[R]
          while (rs.next()) {
            buffer.append(rowMapper(rs))
          }
          buffer.toList
        }
      }),
      needUseTransaction)

  override def findForMap(sql: String, args: Seq[Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Option[Map[String, Object]] =
    findForObject(sql, args, JdbcUtils.resultSetToMap)

  override def findForObject[R](sql: String, args: Seq[Any], rowMapper: ResultSet => R)(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Option[R] =
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(sql),
      JdbcUtils.preparedStatementAction(args, new PreparedStatementAction[Option[R]] {

        override def apply(stmt: PreparedStatement): Option[R] = {
          JdbcUtils.setStatementParameters(stmt, args)
          val rs = stmt.executeQuery()
          if (rs.next()) Option(rowMapper(rs)) else None
        }
      }),
      needUseTransaction)

  override def namedListForMap(sql: String, args: Map[String, Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): List[Map[String, Object]] =
    namedListForObject(sql, args, JdbcUtils.resultSetToMap)

  override def namedListForObject[R](sql: String, args: Map[String, Any], rowMapper: ResultSet => R)(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): List[R] = {
    val (_sql, paramIndex) = JdbcUtils.namedParameterToQuestionMarked(sql)
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(_sql, sql),
      JdbcUtils.preparedStatementAction(args, new PreparedStatementAction[List[R]] {
        override def apply(stmt: PreparedStatement): List[R] = {
          JdbcUtils.setStatementParameters(stmt, args, paramIndex)
          val rs = stmt.executeQuery()
          val buffer = mutable.Buffer.empty[R]
          while (rs.next()) {
            buffer.append(rowMapper(rs))
          }
          buffer.toList
        }
      }),
      needUseTransaction)
  }

  override def namedFindForMap(sql: String, args: Map[String, Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Option[Map[String, Object]] =
    namedFindForObject(sql, args, JdbcUtils.resultSetToMap)

  override def namedFindForObject[R](sql: String, args: Map[String, Any], rowMapper: ResultSet => R)(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Option[R] = {
    val (_sql, paramIndex) = JdbcUtils.namedParameterToQuestionMarked(sql)
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(_sql, sql),
      JdbcUtils.preparedStatementAction(args, new PreparedStatementAction[Option[R]] {
        override def apply(stmt: PreparedStatement): Option[R] = {
          JdbcUtils.setStatementParameters(stmt, args, paramIndex)
          val rs = stmt.executeQuery()
          if (rs.next()) Option(rowMapper(rs)) else None
        }
      }),
      needUseTransaction)
  }

  override def batchUpdate(sql: String, argsList: java.util.Collection[java.util.Collection[Object]]): Array[Int] =
    updateBatch(sql, argsList.asScala.map(_.asScala))

  override def update(sql: String): Int = update(sql, Nil)

  override def update(sql: String, args: Iterable[Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Int =
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(sql),
      JdbcUtils.preparedStatementActionUseUpdate(args),
      needUseTransaction)

  override def javaUpdate(sql: String, args: java.util.Collection[Object], conn: Connection): Int =
    update(sql, args.asScala)

  override def updateBatch(sql: String, argsList: Iterable[Iterable[Any]])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Array[Int] =
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(sql),
      JdbcUtils.preparedStatementActionUseBatchUpdate(argsList),
      needUseTransaction)

  override def namedUpdate(sql: String, args: Map[String, Any])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Int = {
    val (_sql, paramIndex) = JdbcUtils.namedParameterToQuestionMarked(sql)
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(_sql, sql),
      JdbcUtils.preparedStatementActionUseUpdate(args, paramIndex),
      needUseTransaction)
  }

  override def namedUpdateBatch(sql: String, argsList: Iterable[Map[String, Any]])(
      implicit
      connection: Connection = JdbcTemplate.EmptyConnection): Array[Int] = {
    val (_sql, paramIndex) = JdbcUtils.namedParameterToQuestionMarked(sql)
    execute(
      connection,
      JdbcUtils.preparedStatementCreator(_sql, sql),
      JdbcUtils.preparedStatementActionUseBatchUpdate(argsList, paramIndex),
      needUseTransaction)
  }

  override def execute(sql: String): Boolean =
    execute(
      JdbcTemplate.EmptyConnection,
      JdbcUtils.preparedStatementCreator(sql),
      JdbcUtils.preparedStatementAction(Nil, new PreparedStatementAction[Boolean] {
        override def apply(pstmt: PreparedStatement): Boolean = pstmt.execute()
      }),
      needUseTransaction)

  override def execute[R](
      externalConn: Connection,
      pscFunc: ConnectionPreparedStatementCreator,
      actionFunc: PreparedStatementAction[R],
      useTransaction: Boolean): R = {
    assert(Objects.nonNull(pscFunc), "Connection => PreparedStatement must not be null")
    assert(Objects.nonNull(actionFunc), "PreparedStatement => R must not be null")

    val con = if (externalConn == null) dataSource.getConnection else externalConn
    JdbcUtils.execute(pscFunc, actionFunc, ignoreWarnings, allowPrintLog, useTransaction, externalConn == null)(con)
  }
}
