package helloscala.common.util

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

import scala.concurrent.Await
import scala.concurrent.duration._

class DigestUtilsTest extends FunSuite with BeforeAndAfterAll with MustMatchers {

  implicit val system = ActorSystem()
  implicit val mat    = ActorMaterializer()
  import system.dispatcher

  test("digest") {
    fromPath("/opt/Videos/4期,PostgreSQL_master_slave_HA_2VIP-5.avi") mustBe fromPathFuture(
      "/opt/Videos/4期,PostgreSQL_master_slave_HA_2VIP-5.avi")
    fromPath("/opt/Music/Akon-Lonely.flac") mustBe fromPathFuture("/opt/Music/Akon-Lonely.flac")
    fromPath("/opt/Videos/postgresql-上海-d2-6物理备份的还原.avi") mustBe fromPathFuture(
      "/opt/Videos/postgresql-上海-d2-6物理备份的还原.avi")
    fromPath("/opt/Desktop/红卡专辑：火灾/汽车起火如何逃生1.m4a") mustBe fromPathFuture("/opt/Desktop/红卡专辑：火灾/汽车起火如何逃生1.m4a")
  }

  def fromPath(path: String)       = DigestUtils.sha256HexFromPath(Paths.get(path))
  def fromPathFuture(path: String) = Await.result(DigestUtils.reactiveSha256Hex(Paths.get(path)), 10.seconds)

  override protected def afterAll(): Unit = {
    system.dispatcher
  }

}
