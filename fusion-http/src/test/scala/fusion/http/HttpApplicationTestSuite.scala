package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import fusion.test.FusionTestSuite
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class HttpApplicationTestSuite extends FusionTestSuite with BeforeAndAfterAll {

  private var _system: ActorSystem = _
  private var _materializer: ActorMaterializer = _

  protected var binding: ServerBinding = _

  implicit protected def system: ActorSystem = _system
  implicit protected def materializer: ActorMaterializer = _materializer

  protected def createActorSystem() = ActorSystem("fusion-test")

  protected def createActorMaterializer(): ActorMaterializer = ActorMaterializer()(_system)

  override protected def beforeAll(): Unit = {
    _system = createActorSystem()
    _materializer = createActorMaterializer()
    val routes = new Routes()
    val (f, _) = HttpApplication(_system, routes).startServer()
    binding = Await.result(f, Duration.Inf)
  }

  override protected def afterAll(): Unit = {
    import scala.concurrent.duration._
    Await.ready(binding.unbind(), Duration.Inf)
    binding.terminate(10.seconds)
  }

}
