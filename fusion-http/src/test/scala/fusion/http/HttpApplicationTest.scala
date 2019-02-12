package fusion.http

import org.scalatest.{BeforeAndAfterAll, FunSuite, MustMatchers}

import scala.concurrent.Future

class HttpApplicationTest extends FunSuite with MustMatchers with BeforeAndAfterAll {
  private var binding: Future[_] = _

  test("") {}

  override protected def beforeAll(): Unit = {}
  override protected def afterAll(): Unit = {}
}
