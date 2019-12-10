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

package fusion.discoveryx.server.route

import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import fusion.discoveryx.common.Constants

import scala.concurrent.Future

class Routes(grpcHandler: HttpRequest => Future[HttpResponse]) {
  def route: Route =
    pathPrefix("fusion" / Constants.DISCOVERYX / "v1") {
      namingRoute ~
      configRoute
    } ~
    extractRequest { request =>
      onSuccess(grpcHandler(request)) { response =>
        complete(response)
      }
    }

  def namingRoute: Route = pathPrefix("naming") {
    complete(StatusCodes.NotImplemented)
  }

  def configRoute: Route = pathPrefix("config") {
    complete(StatusCodes.NotImplemented)
  }
}
