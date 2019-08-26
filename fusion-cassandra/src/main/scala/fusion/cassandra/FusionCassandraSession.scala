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

import java.util.Objects

import com.datastax.oss.driver.api.core.CqlSessionBuilder
import com.datastax.oss.driver.api.core.`type`.reflect.GenericType
import com.datastax.oss.driver.api.core.cql.AsyncResultSet
import com.datastax.oss.driver.api.core.cql.PrepareRequest
import com.datastax.oss.driver.api.core.cql.PreparedStatement
import com.datastax.oss.driver.api.core.cql.ResultSet
import com.datastax.oss.driver.api.core.cql.SimpleStatement
import com.datastax.oss.driver.api.core.cql.Statement
import com.datastax.oss.driver.api.core.session.Request

import scala.compat.java8.FutureConverters._
import com.datastax.oss.driver.internal.core.cql.DefaultPrepareRequest
import edu.umd.cs.findbugs.annotations.NonNull
import edu.umd.cs.findbugs.annotations.Nullable

import scala.concurrent.Future

trait FusionCassandraSession {

  @Nullable def execute[RequestT <: Request, ResultT](
      @NonNull request: RequestT,
      @NonNull resultType: GenericType[ResultT]): ResultT

  /** Returns a builder to create a new instance. */
  @NonNull def builder = new CqlSessionBuilder

  /**
   * Executes a CQL statement synchronously (the calling thread blocks until the result becomes
   * available).
   */
  @NonNull def execute(@NonNull statement: Statement[_]): ResultSet =
    Objects.requireNonNull(execute(statement, Statement.SYNC), "The CQL processor should never return a null result")

  @NonNull def execute(@NonNull query: String): ResultSet = execute(SimpleStatement.newInstance(query))

  /**
   * Executes a CQL statement asynchronously (the call returns as soon as the statement was sent,
   * generally before the result is available).
   */
  @NonNull def executeAsync(@NonNull statement: Statement[_]): Future[AsyncResultSet] =
    Objects
      .requireNonNull(execute(statement, Statement.ASYNC), "The CQL processor should never return a null result")
      .toScala

  @NonNull def executeAsync(@NonNull query: String): Future[AsyncResultSet] =
    executeAsync(SimpleStatement.newInstance(query))

  /**
   * Prepares a CQL statement synchronously (the calling thread blocks until the statement is
   * prepared).
   *
   * <p>Note that the bound statements created from the resulting prepared statement will inherit
   * some of the attributes of the provided simple statement. That is, given:
   *
   * <pre>{@code
   * SimpleStatement simpleStatement = SimpleStatement.newInstance("...");
   * PreparedStatement preparedStatement = session.prepare(simpleStatement);
   * BoundStatement boundStatement = preparedStatement.bind();
   * }</pre>
   *
   * Then:
   *
   * <ul>
   * <li>the following methods return the same value as their counterpart on {@code
   * simpleStatement}:
   * <ul>
   * <li>{@link Request#getExecutionProfileName() boundStatement.getExecutionProfileName()}
   * <li>{@link Request#getExecutionProfile() boundStatement.getExecutionProfile()}
   * <li>{@link Statement#getPagingState() boundStatement.getPagingState()}
   * <li>{@link Request#getRoutingKey() boundStatement.getRoutingKey()}
   * <li>{@link Request#getRoutingToken() boundStatement.getRoutingToken()}
   * <li>{@link Request#getCustomPayload() boundStatement.getCustomPayload()}
   * <li>{@link Request#isIdempotent() boundStatement.isIdempotent()}
   * <li>{@link Request#getTimeout() boundStatement.getTimeout()}
   * <li>{@link Statement#getPagingState() boundStatement.getPagingState()}
   * <li>{@link Statement#getPageSize() boundStatement.getPageSize()}
   * <li>{@link Statement#getConsistencyLevel() boundStatement.getConsistencyLevel()}
   * <li>{@link Statement#getSerialConsistencyLevel()
   *             boundStatement.getSerialConsistencyLevel()}
   * <li>{@link Statement#isTracing() boundStatement.isTracing()}
   * </ul>
   * <li>{@link Request#getRoutingKeyspace() boundStatement.getRoutingKeyspace()} is set from
   * either {@link Request#getKeyspace() simpleStatement.getKeyspace()} (if it's not {@code
   * null}), or {@code simpleStatement.getRoutingKeyspace()};
   * <li>on the other hand, the following attributes are <b>not</b> propagated:
   * <ul>
   * <li>{@link Statement#getQueryTimestamp() boundStatement.getQueryTimestamp()} will be
   * set to {@link Long#MIN_VALUE}, meaning that the value will be assigned by the
   * session's timestamp generator.
   * <li>{@link Statement#getNode() boundStatement.getNode()} will always be {@code null}.
   * </ul>
   * </ul>
   *
   * If you want to customize this behavior, you can write your own implementation of {@link
   * PrepareRequest} and pass it to {@link #prepare(PrepareRequest)}.
   *
   * <p>The result of this method is cached: if you call it twice with the same {@link
   * SimpleStatement}, you will get the same {@link PreparedStatement} instance. We still recommend
   * keeping a reference to it (for example by caching it as a field in a DAO); if that's not
   * possible (e.g. if query strings are generated dynamically), it's OK to call this method every
   * time: there will just be a small performance overhead to check the internal cache. Note that
   * caching is based on:
   *
   * <ul>
   * <li>the query string exactly as you provided it: the driver does not perform any kind of
   * trimming or sanitizing.
   * <li>all other execution parameters: for example, preparing two statements with identical
   * query strings but different {@linkplain SimpleStatement#getConsistencyLevel() consistency
   *       levels} will yield distinct prepared statements.
   * </ul>
   */
  @NonNull def prepare(@NonNull statement: SimpleStatement): PreparedStatement =
    Objects.requireNonNull(
      execute(new DefaultPrepareRequest(statement), PrepareRequest.SYNC),
      "The CQL prepare processor should never return a null result")

  /**
   * Prepares a CQL statement synchronously (the calling thread blocks until the statement is
   * prepared).
   *
   * <p>The result of this method is cached (see {@link #prepare(SimpleStatement)} for more
   * explanations).
   */
  @NonNull def prepare(@NonNull query: String): PreparedStatement =
    Objects.requireNonNull(
      execute(new DefaultPrepareRequest(query), PrepareRequest.SYNC),
      "The CQL prepare processor should never return a null result")

  /**
   * Prepares a CQL statement synchronously (the calling thread blocks until the statement is
   * prepared).
   *
   * <p>This variant is exposed in case you use an ad hoc {@link PrepareRequest} implementation to
   * customize how attributes are propagated when you prepare a {@link SimpleStatement} (see {@link
   * #prepare(SimpleStatement)} for more explanations). Otherwise, you should rarely have to deal
   * with {@link PrepareRequest} directly.
   *
   * <p>The result of this method is cached (see {@link #prepare(SimpleStatement)} for more
   * explanations).
   */
  @NonNull def prepare(@NonNull request: PrepareRequest): PreparedStatement =
    Objects.requireNonNull(
      execute(request, PrepareRequest.SYNC),
      "The CQL prepare processor should never return a null result")

  /**
   * Prepares a CQL statement asynchronously (the call returns as soon as the prepare query was
   * sent, generally before the statement is prepared).
   *
   * <p>Note that the bound statements created from the resulting prepared statement will inherit
   * some of the attributes of {@code query}; see {@link #prepare(SimpleStatement)} for more
   * details.
   *
   * <p>The result of this method is cached (see {@link #prepare(SimpleStatement)} for more
   * explanations).
   */
  @NonNull def prepareAsync(@NonNull statement: SimpleStatement): Future[PreparedStatement] =
    Objects
      .requireNonNull(
        execute(new DefaultPrepareRequest(statement), PrepareRequest.ASYNC),
        "The CQL prepare processor should never return a null result")
      .toScala

  /**
   * Prepares a CQL statement asynchronously (the call returns as soon as the prepare query was
   * sent, generally before the statement is prepared).
   *
   * <p>The result of this method is cached (see {@link #prepare(SimpleStatement)} for more
   * explanations).
   */
  @NonNull def prepareAsync(@NonNull query: String): Future[PreparedStatement] =
    Objects
      .requireNonNull(
        execute(new DefaultPrepareRequest(query), PrepareRequest.ASYNC),
        "The CQL prepare processor should never return a null result")
      .toScala

  /**
   * Prepares a CQL statement asynchronously (the call returns as soon as the prepare query was
   * sent, generally before the statement is prepared).
   *
   * <p>This variant is exposed in case you use an ad hoc {@link PrepareRequest} implementation to
   * customize how attributes are propagated when you prepare a {@link SimpleStatement} (see {@link
   * #prepare(SimpleStatement)} for more explanations). Otherwise, you should rarely have to deal
   * with {@link PrepareRequest} directly.
   *
   * <p>The result of this method is cached (see {@link #prepare(SimpleStatement)} for more
   * explanations).
   */
  @NonNull def prepareAsync(request: PrepareRequest): Future[PreparedStatement] =
    Objects
      .requireNonNull(
        execute(request, PrepareRequest.ASYNC),
        "The CQL prepare processor should never return a null result")
      .toScala
}
