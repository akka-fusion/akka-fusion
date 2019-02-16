package fusion.docs.sample

import akka.http.scaladsl.server.Route
import fusion.core.inject.Injects
import fusion.http.FusionServer
import fusion.http.server.AbstractRoute
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

// #SampleApplication
object SampleApplication extends App {
  Injects.instance[SampleServer].start()
}

// Server
@Singleton
class SampleServer @Inject()(val routes: SampleRoute) extends FusionServer

// Controller
@Singleton
class SampleRoute @Inject()(sampleService: SampleService) extends AbstractRoute {
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
@Singleton
class SampleService @Inject()(implicit ec: ExecutionContext) {
  def hello(req: SampleReq): Future[SampleResp] = Future {
    SampleResp(req.hello, req.year, "scala")
  }
}
// #SampleApplication
