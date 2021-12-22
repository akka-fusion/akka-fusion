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

package helloscala.jdbc

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.zaxxer.hikari.HikariDataSource
import fusion.testkit.FusionFunSuiteLike
import helloscala.jdbc.util.JdbcUtils

import java.time.OffsetDateTime

case class AfUser(
    var id: java.lang.Long,
    var name: String = "",
    var age: Integer = 0,
    var sex: Integer = 1,
    var permissions: Array[Int] = Array(),
    var avatarUrl: String = "",
    var status: Integer = 0,
    var createBy: OffsetDateTime = OffsetDateTime.now(),
    var updateTime: OffsetDateTime = OffsetDateTime.now(),
    var deleted: Integer = 0) {
  def this() = this(0L)
}

// #JdbcTemplateTest
class JdbcTemplateTest extends ScalaTestWithActorTestKit with FusionFunSuiteLike {
  private def dataSource: HikariDataSource =
    JdbcUtils.createHikariDataSource(system.settings.config.getConfig("fusion.jdbc.pg-primary"))
  private def jdbcTemplate = JdbcTemplate(dataSource)

  test("listForObject") {
    val sql =
      "select id, name, age, sex, permissions, avatar_url, status, create_time, update_time, deleted from af_user.\"user\";"
    val list = jdbcTemplate.listForObject(sql, Nil, JdbcUtils.resultSetToBean[AfUser])
    list.foreach(println)
    println(list.size)
    println("---------------------")
  }

  test("insert") {
    val sql =
      """insert into af_user."user"(id, name, age, sex, permissions, avatar_url, status, create_time, update_time, deleted)
                |values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""".stripMargin
    val user = AfUser(1, "羊八井", 36)
    val ret = jdbcTemplate.update(sql, user.productIterator.toList)
    ret shouldBe 1
  }

  override protected def afterAll(): Unit = {
    dataSource.close()
    super.afterAll()
  }
}
// #JdbcTemplateTest
