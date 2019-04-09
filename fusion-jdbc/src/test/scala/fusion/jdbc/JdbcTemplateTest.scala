package fusion.jdbc

import java.time.OffsetDateTime

import akka.actor.ActorSystem
import com.zaxxer.hikari.HikariDataSource
import fusion.jdbc.util.JdbcUtils
import fusion.test.FusionTestFunSuite
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class CFile {
  var fileId: String                  = _
  var fileSubject: String             = _
  var fileType: java.lang.Integer     = _
  var fileUrl: String                 = _
  var fileCtime: java.lang.Long       = _
  var tableAutoUptime: OffsetDateTime = _
  var duration: java.lang.Integer     = _
  var fileSize: java.lang.Integer     = _

  override def toString =
    s"CFile($fileId, $fileSubject, $fileType, $fileUrl, $fileCtime, $tableAutoUptime, $duration, $fileSize)"
}

// #JdbcTemplateTest
class JdbcTemplateTest extends FusionTestFunSuite with BeforeAndAfterAll {
  private val system                       = ActorSystem()
  private def dataSource: HikariDataSource = FusionJdbc(system).component
  private def jdbcTemplate                 = JdbcTemplate(dataSource)

  test("listForObject") {
    val sql =
      "select file_id, file_subject, file_type, file_url, file_ctime, table_auto_uptime, duration, file_size from c_file where table_auto_uptime is null or file_size is null limit 50"
    val list = jdbcTemplate.listForObject(sql, Nil, JdbcUtils.resultSetToBean[CFile])
    list.foreach(println)
    println(list.size)
    println("---------------------")
  }

//  test("insertOne") {
//    val sql  = """insert into c_file(file_id, file_subject, file_type, file_url, file_ctime, duration, hash)
//                |values (?, ?, ?, ?, ?, ?, ?);""".stripMargin
//    val hash = DigestUtils.sha256Hex(Random.nextString(12))
//    val ret =
//      jdbcTemplate.update(
//        sql,
//        List(hash, "subject", 3, s"/${hash.take(2)}/$hash", System.currentTimeMillis(), 23432, hash))
//    ret mustBe 1
//  }

//  test("selectAll") {
//    //中华人民共和国
//    val list = jdbcTemplate.listForMap("select * from c_file order by file_ctime desc", Nil)
//    list must not be empty
//    list.foreach(println)
//  }

  override protected def afterAll(): Unit = {
    system.terminate()
    Await.ready(system.whenTerminated, Duration.Inf)
  }

}
// #JdbcTemplateTest
