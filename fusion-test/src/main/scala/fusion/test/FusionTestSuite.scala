package fusion.test

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.Millis
import org.scalatest.time.Span

import scala.concurrent.duration._

trait FusionTestSuite extends MustMatchers with OptionValues with EitherValues with ScalaFutures {
  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(patienceTimeout.toMillis, Millis)), scaled(Span(15, Millis)))

  protected def patienceTimeout: FiniteDuration = 2.second
}

trait FusionTestFunSuite extends FunSuiteLike with FusionTestSuite

trait FusionTestWordSpec extends WordSpecLike with FusionTestSuite
