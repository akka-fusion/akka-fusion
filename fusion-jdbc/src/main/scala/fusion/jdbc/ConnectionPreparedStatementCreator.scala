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

package fusion.jdbc

import java.sql.Connection
import java.sql.PreparedStatement

import helloscala.common.util.StringUtils

@FunctionalInterface
trait ConnectionPreparedStatementCreator {
  def apply(conn: Connection): PreparedStatement
}

class ConnectionPreparedStatementCreatorImpl(sql: String, namedSql: String = "")
    extends ConnectionPreparedStatementCreator {
  def getSql: String = if (StringUtils.isNoneBlank(namedSql)) namedSql else sql

  override def apply(conn: Connection): PreparedStatement = conn.prepareStatement(sql)
}
