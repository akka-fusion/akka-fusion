package fusion.jdbc

import akka.actor.ActorSystem
import com.zaxxer.hikari.HikariDataSource
import fusion.test.FusionTestFunSuite
import helloscala.common.util.DigestUtils
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Random

// #JdbcTemplateTest
class JdbcTemplateTest extends FusionTestFunSuite with BeforeAndAfterAll {
  private val system = ActorSystem()
  private def dataSource: HikariDataSource = FusionJdbc(system).component
  private def jdbcTemplate = JdbcTemplate(dataSource)

  test("insertOne") {
    val sql = """insert into c_file(file_id, file_subject, file_type, file_url, file_ctime, duration, hash)
                |values (?, ?, ?, ?, ?, ?, ?);""".stripMargin
    val hash = DigestUtils.sha256Hex(Random.nextString(12))
    val ret =
      jdbcTemplate.update(sql,
                          List(hash, "subject", 3, s"/${hash.take(2)}/$hash", System.currentTimeMillis(), 23432, hash))
    ret mustBe 1
  }

  test("selectAll") {
    //中华人民共和国
    val list = jdbcTemplate.listForMap("select * from c_file order by file_ctime desc", Nil)
    list must not be empty
    list.foreach(println)
  }

  override protected def afterAll(): Unit = {
    system.terminate()
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}
// #JdbcTemplateTest
