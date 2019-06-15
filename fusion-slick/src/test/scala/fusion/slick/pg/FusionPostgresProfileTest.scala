package fusion.slick.pg

import java.time.LocalDateTime

import akka.actor.ActorSystem
import fusion.jdbc.FusionJdbc
import akka.testkit.TestKit
import fusion.jdbc.util.JdbcUtils
import fusion.test.FusionTestFunSuite
import helloscala.common.jackson.Jackson
import helloscala.common.util.Utils

import fusion.slick.FusionPostgresProfile.api._

case class Test(id: Int, name: String, sex: Option[Int], createdAt: LocalDateTime)

class TableTest(tag: Tag) extends Table[Test](tag, "t_test") {
  val id         = column[Int]("id", O.PrimaryKey, O.AutoInc)
  val name       = column[String]("name")
  val sex        = column[Option[Int]]("sex")
  val createdAt  = column[LocalDateTime]("created_at")
  override def * = (id, name, sex, createdAt).mapTo[Test]
}

class FusionPostgresProfileTest extends TestKit(ActorSystem()) with FusionTestFunSuite {

  test("init jdbc") {
    val dataSource = FusionJdbc(system).component
    Utils.using(dataSource.getConnection) { conn =>
      val metaData = conn.getMetaData
      val info     = JdbcUtils.getDatabaseInfo(metaData)
      println(Jackson.prettyStringify(info))
    }
  }

  test("test") {
    val db    = Database.forDataSource(FusionJdbc(system).component, None)
    val tTest = TableQuery[TableTest]
    val name  = "杨景"
    val query = tTest.filter(t => t.name.like(s"%$name%")).result
    val list  = db.run(query).futureValue
    list.foreach(println)
  }
}
