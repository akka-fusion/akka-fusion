package fusion.core.extension

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import org.scalatest.FunSuite
import org.scalatest.MustMatchers

class FusionCoreTest extends FunSuite with MustMatchers with BeforeAndAfterAll {
  val system = ActorSystem()
  test("core") {
    val core = FusionCore(system)
    println(core.configuration)
    println(core.configuration)
  }

  override protected def afterAll(): Unit = { system.terminate() }
}
