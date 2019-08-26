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

package fusion.mybatis

import java.sql.Connection

import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.TransactionIsolationLevel

import scala.reflect.ClassTag

final class FusionSqlSessionFactory(underlying: SqlSessionFactory) extends SqlSessionFactory {
  override def openSession(): SqlSession = underlying.openSession()

  override def openSession(autoCommit: Boolean): SqlSession = underlying.openSession(autoCommit)

  override def openSession(connection: Connection): SqlSession = underlying.openSession(connection)

  override def openSession(level: TransactionIsolationLevel): SqlSession = underlying.openSession(level)

  override def openSession(execType: ExecutorType): SqlSession = underlying.openSession(execType)

  override def openSession(execType: ExecutorType, autoCommit: Boolean): SqlSession =
    underlying.openSession(execType, autoCommit)

  override def openSession(execType: ExecutorType, level: TransactionIsolationLevel): SqlSession =
    underlying.openSession(execType, level)

  override def openSession(execType: ExecutorType, connection: Connection): SqlSession =
    underlying.openSession(execType, connection)

  override def getConfiguration: Configuration = underlying.getConfiguration

  def session[T](func: SqlSession => T): T = {
    val session = openSession()
    try {
      func(session)
    } finally {
      session.close()
    }
  }

  def mapperClassTransactional[M, R](mapperClass: Class[M], func: M => R): R = {
    transactional(session => func(session.getMapper(mapperClass)))
  }

  def mapperTransactional[M, R](func: M => R)(implicit mapperClassTag: ClassTag[M]): R = {
    val mc: Class[M] = mapperClassTag.runtimeClass.asInstanceOf[Class[M]]
    mapperClassTransactional(mc, func)
  }

  def transactional[T](func: SqlSession => T): T = {
    val session = openSession()
    try {
      val result = func(session)
      session.commit()
      result
    } catch {
      case e: Throwable =>
        session.rollback()
        throw e
    } finally {
      session.close()
    }
  }

}
