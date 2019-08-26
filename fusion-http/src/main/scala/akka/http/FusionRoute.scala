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

package akka.http

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings.ParserSettings
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

object FusionRoute {

  /**
   * Turns a `Route` into an async handler function.
   */
  def asyncHandler(route: Route)(
      implicit
      routingSettings: RoutingSettings,
      parserSettings: ParserSettings,
      materializer: ActorMaterializer,
      routingLog: RoutingLog,
      executionContext: ExecutionContextExecutor = null): HttpRequest => Future[HttpResponse] = {
    import akka.http.scaladsl.util.FastFuture._
    val effectiveEC = if (executionContext ne null) executionContext else materializer.executionContext

    {
      implicit val executionContext = effectiveEC // overrides parameter
      val effectiveParserSettings = if (parserSettings ne null) parserSettings else ParserSettings(materializer.system)
      val sealedRoute = route
      request =>
        sealedRoute(new RequestContextImpl(
          request,
          routingLog.requestLog(request),
          routingSettings,
          effectiveParserSettings)).fast.map {
          case RouteResult.Complete(response) => response
          case RouteResult.Rejected(rejected) =>
            throw new IllegalStateException(s"Unhandled rejections '$rejected', unsealed RejectionHandler?!")
        }
    }
  }

}
