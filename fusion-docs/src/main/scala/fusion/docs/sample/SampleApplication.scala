package fusion.docs.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import fusion.http.FusionHttp
import fusion.http.server.AbstractRoute
import fusion.starter.http.FusionServer

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

// #SampleApplication
object SampleApplication {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val ec     = system.dispatcher
    val sampleService   = new SampleService()
    val routes          = new SampleRoute(sampleService)
    FusionHttp(system).startAwait(routes.route)
  }
}

// Server
class SampleServer(val routes: SampleRoute) extends FusionServer

// Controller
class SampleRoute(sampleService: SampleService) extends AbstractRoute {
  override def route: Route = pathGet("hello") {
    parameters(('hello, 'year.as[Int].?(2019))).as(SampleReq) { req =>
      futureComplete(sampleService.hello(req))
    }
  }
}

// Request、Response Model
case class SampleReq(hello: String, year: Int)
case class SampleResp(hello: String, year: Int, language: String)

// Service
class SampleService()(implicit ec: ExecutionContext) {

  def hello(req: SampleReq): Future[SampleResp] = Future {
    SampleResp(req.hello, req.year, "scala")
  }
}
// #SampleApplication
