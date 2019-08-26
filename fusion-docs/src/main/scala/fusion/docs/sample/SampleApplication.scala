/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.docs.sample

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import fusion.http.FusionHttpServer
import fusion.http.server.AbstractRoute

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

// #SampleApplication
object SampleApplication {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val ec = system.dispatcher
    val sampleService = new SampleService()
    val routes = new SampleRoute(sampleService)
    FusionHttpServer(system).component.startRouteSync(routes.route)
  }
}

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
