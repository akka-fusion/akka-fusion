package fusion.test

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}

trait FusionTestFunSuite extends FunSuiteLike with MustMatchers with OptionValues with EitherValues with ScalaFutures {
  implicit override def patienceConfig: PatienceConfig =
    PatienceConfig(scaled(Span(1000, Millis)), scaled(Span(15, Millis)))
}
