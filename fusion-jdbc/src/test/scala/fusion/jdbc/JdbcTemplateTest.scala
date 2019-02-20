package fusion.jdbc

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import fusion.jdbc.util.JdbcUtils
import fusion.test.FusionTestFunSuite
import helloscala.common.util.DigestUtils
import org.scalatest.BeforeAndAfterAll

import scala.util.Random

class JdbcTemplateTest extends FusionTestFunSuite with BeforeAndAfterAll {
  var dataSource: HikariDataSource = _

  test("insertOne") {
    val sql = """insert into c_file(file_id, file_subject, file_type, file_url, file_ctime, duration, hash)
                |values (?, ?, ?, ?, ?, ?, ?);""".stripMargin
    val hash = DigestUtils.sha256Hex(Random.nextString(12))
    val ret = JdbcTemplate(dataSource).update(
      sql,
      List(
        hash,
        "subject",
        3,
        s"/${hash.take(2)}/$hash",
        System.currentTimeMillis(),
//                                                   LocalDateTime.of(1999, 1, 1, 1, 1, 1, 1),
        23432,
        hash
      )
    )
    ret mustBe 1
  }

  test("selectIsEmpty") {
    val list = JdbcTemplate(dataSource).listForMap("select * from c_file order by file_ctime desc", Nil)
    list must not be empty
    list.foreach(println)
  }

  override protected def beforeAll(): Unit = {
    val config = ConfigFactory.load()

    dataSource = JdbcUtils.createHikariDataSource(config.getConfig("fusion.jdbc.default"))
    dataSource must not be null
    println(dataSource)
    println(dataSource.getConnectionInitSql)
    println(dataSource.getConnectionTestQuery)
  }

  override protected def afterAll(): Unit = {
    dataSource.close()
  }
}
