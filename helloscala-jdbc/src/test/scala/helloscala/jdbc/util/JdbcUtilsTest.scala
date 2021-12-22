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

package helloscala.jdbc.util

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.time.Instant

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since v1.1 2021-09-15 21:21:03
 */
class JdbcUtilsTest extends AnyWordSpec with Matchers {

  "JdbcUtilsTest" should {
    "handleSqlLogs" in {
      val types = Seq("int4", "varchar", "float8")
      val psFunc = JdbcUtils.preparedStatementCreator("insert into test(id, text, amount) values(?, ?, )")
      val actionFunc = JdbcUtils.preparedStatementActionUseBatchUpdate(Seq(Seq(1, "aaa", 3.3), Seq(2, "bbb", 43.2)))
      JdbcUtils.handleSqlLogs(Instant.now(), types, psFunc, actionFunc)
    }

    "namedParameterToQuestionMarked" in {
      val sql = "insert into test(name, age, sex) values(:name, :age, '1'::int)"
      val (realSql, data) = JdbcUtils.namedParameterToQuestionMarked(sql)
      println(realSql)
      println(data)
    }
  }
}
