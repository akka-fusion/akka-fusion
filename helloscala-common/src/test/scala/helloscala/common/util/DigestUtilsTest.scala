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

  val PROJECT_BASE = sys.props("user.dir")

  test("digest") {
    fromPath(s"$PROJECT_BASE/README.md") mustBe fromPathFuture(s"$PROJECT_BASE/README.md")
  }

  private def fromPath(path: String)       = DigestUtils.sha256HexFromPath(Paths.get(path))
  private def fromPathFuture(path: String) = Await.result(DigestUtils.reactiveSha256Hex(Paths.get(path)), 10.seconds)

  override protected def afterAll(): Unit = {
    system.dispatcher
  }

}
