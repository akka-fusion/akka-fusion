package fusion.docs.example

import java.util.concurrent.TimeUnit

import akka.Done
import akka.actor.ActorSystem
import akka.actor.CoordinatedShutdown
import com.typesafe.scalalogging.StrictLogging
import fusion.test.FusionTestFunSuite
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class TerminateTest extends FusionTestFunSuite with BeforeAndAfterAll with StrictLogging {
  private val system = ActorSystem()
  import system.dispatcher

  test("init") {
    val coordinatedShutdown = CoordinatedShutdown(system)
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeServiceUnbind, "serviceUnbind") { () =>
      logger.info("---serviceUnbind")
      Future {
        TimeUnit.SECONDS.sleep(1)
        logger.info("serviceUnbind")
        Done
      }
    }
    coordinatedShutdown.addTask(CoordinatedShutdown.PhaseBeforeActorSystemTerminate, "beforeTerminate") { () =>
      logger.info("---beforeTerminate")
      Future {
        TimeUnit.SECONDS.sleep(1)
        logger.info("beforeTerminate")
        Done
      }
    }
    TimeUnit.SECONDS.sleep(2)
    logger.info("ddddddd")
  }

  test("terminate") {
    TimeUnit.SECONDS.sleep(30)
    val begin = System.nanoTime()
    CoordinatedShutdown(system).run(CoordinatedShutdown.UnknownReason)
//    system.terminate()
    Await.result(system.whenTerminated, Duration.Inf)
//    System.exit(0)
    val done = System.nanoTime() - begin
    logger.info("terminate is " + java.time.Duration.ofNanos(done))
  }
}
