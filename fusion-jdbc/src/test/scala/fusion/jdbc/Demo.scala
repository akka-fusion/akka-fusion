package fusion.jdbc

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Source
import helloscala.common.util.StringUtils
import helloscala.common.util.Utils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Demo {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem()
    try {
      val (_, t) = Utils.timing {
        perform(system)
      }
      println(s"time: $t")
    } finally {
      system.terminate()
    }
  }

  private def perform(system: ActorSystem): Unit = {
    val ds           = FusionJdbc(system).components.lookup("fusion.jdbc.dddd")
    val jdbcTemplate = JdbcTemplate(ds)

    val phoneLists = scala.io.Source
      .fromFile("/home/yangjing/select_login_phone_count____from_u_user_.tsv")
      .getLines()
      .filter(StringUtils.isNoneBlank)
      .map(line => line.split("\t"))
      .filter(_.length > 1)
      .map(_.apply(0))
//      .map(phone => List(phone))
//      .toList
//    jdbcTemplate.withTransaction { implicit conn =>
//      phones.foreach { phone =>
//        jdbcTemplate.update("delete from u_user_credential_bak where login_phone = ? and salt is null;", List(phone))
//      }
//    }
    implicit val mat = ActorMaterializer()(system)

    val f = Source(phoneLists.toList).grouped(100).async.runForeach { phones =>
      val ret = jdbcTemplate.updateBatch(
        "delete from u_user_credential where login_phone = ? and salt is null;",
        phones.map(phone => List(phone)))
      println(java.util.Arrays.toString(ret))
    }

    Await.result(f, Duration.Inf)
  }
}
