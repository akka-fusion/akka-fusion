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

package fusion.slick.pg

import java.time.LocalDateTime

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import fusion.jdbc.FusionJdbc
import fusion.jdbc.util.JdbcUtils
import fusion.json.jackson.Jackson
import fusion.slick.FusionPostgresProfile.api._
import fusion.test.FusionFunSuiteLike
import helloscala.common.util.Utils

case class Test(id: Int, name: String, sex: Option[Int], createdAt: LocalDateTime)

class TableTest(tag: Tag) extends Table[Test](tag, "t_test") {
  val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val name = column[String]("name")
  val sex = column[Option[Int]]("sex")
  val createdAt = column[LocalDateTime]("created_at")
  override def * = (id, name, sex, createdAt).mapTo[Test]
}

class FusionPostgresProfileTest extends ScalaTestWithActorTestKit with FusionFunSuiteLike {
  test("init jdbc") {
    val dataSource = FusionJdbc(system).component
    Utils.using(dataSource.getConnection) { conn =>
      val metaData = conn.getMetaData
      val info = JdbcUtils.getDatabaseInfo(metaData)
      println(Jackson.prettyStringify(info))
    }
  }

  test("test") {
    val db = Database.forDataSource(FusionJdbc(system).component, None)
    val tTest = TableQuery[TableTest]
    val name = "杨景"
    val query = tTest.filter(t => t.name.like(s"%$name%")).result
    val list = db.run(query).futureValue
    list.foreach(println)
  }
}
