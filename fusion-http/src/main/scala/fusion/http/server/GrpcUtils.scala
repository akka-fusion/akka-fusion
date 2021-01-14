/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.http.server

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.Future

object GrpcUtils extends StrictLogging {

  def contactToRoute(partials: PartialFunction[HttpRequest, Future[HttpResponse]]*): HttpRequest => Route = {
    contactToRouteCustom(partials: _*) {
      case req =>
        logger.warn(s"gRPC handler not found, $req")
        reject
    }
  }

  def contactToRouteCustom(
      partials: PartialFunction[HttpRequest, Future[HttpResponse]]*
  )(rejectPF: PartialFunction[HttpRequest, Route]): HttpRequest => Route = {
    partials
      .foldLeft(PartialFunction.empty[HttpRequest, Future[HttpResponse]]) {
        case (acc, pf) =>
          acc.orElse(pf)
      }
      .andThen(f =>
        onSuccess(f) { response =>
          complete(response)
        }
      )
      .orElse(rejectPF)
  }
}
