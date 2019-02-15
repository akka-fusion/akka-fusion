package fusion.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import fusion.test.FusionTestFunSuite
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.Await
import scala.concurrent.duration.Duration

abstract class HttpApplicationTestSuite extends FusionTestFunSuite with BeforeAndAfterAll {

  private var _system: ActorSystem = _
  private var _materializer: ActorMaterializer = _

  protected var binding: ServerBinding = _

  implicit protected def system: ActorSystem = _system
  implicit protected def materializer: ActorMaterializer = _materializer

  def httpApplication: HttpApplication = FusionHttp(system).httpApplication
  def serverHost: String = httpApplication.serverHost
  def serverPort: Int = httpApplication.serverPort

  protected def createActorSystem() = ActorSystem("fusion-test")

  protected def createActorMaterializer(): ActorMaterializer = ActorMaterializer()(_system)

  def createRoute: Route

  override protected def beforeAll(): Unit = {
    _system = createActorSystem()
    _materializer = createActorMaterializer()
    FusionHttp(system).startAwait(createRoute)
    val (f, _) = httpApplication.startServer()
    binding = Await.result(f, Duration.Inf)
  }

  override protected def afterAll(): Unit = {
    import scala.concurrent.duration._
    Await.ready(binding.unbind(), Duration.Inf)
    binding.terminate(10.seconds)
  }

}
