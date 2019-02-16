package fusion.test

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}

import scala.concurrent.duration._

trait FusionTestFunSuite extends FunSuiteLike with MustMatchers with OptionValues with EitherValues with ScalaFutures {
  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(patienceTimeout.toMillis, Millis)), scaled(Span(15, Millis)))

  protected def patienceTimeout: FiniteDuration = 1.second
}
