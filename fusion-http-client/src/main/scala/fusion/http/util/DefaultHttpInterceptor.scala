/*
 * Copyright 2019 akka-fusion.com
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

package fusion.http.util

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse }
import akka.http.scaladsl.server.{ Route, RouteResult }
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.FusionConstants
import fusion.core.extension.FusionCore
import fusion.core.http.headers.{ `X-Request-Time`, `X-Service`, `X-Span-Time` }
import fusion.http.interceptor.HttpInterceptor
import helloscala.common.exception.HSInternalErrorException

final class DefaultHttpInterceptor(system: ActorSystem[_]) extends HttpInterceptor with StrictLogging {
  import system.executionContext
  private val core = FusionCore(system)

  override def interceptor(inner: Route): Route = { ctx =>
    val req = ctx.request
    val extHeaders = List(
      if (req.headers.exists(_.name() == `X-Request-Time`.name)) None
      else Some(`X-Request-Time`.fromInstantNow()),
      if (req.headers.exists(header => header.name() == FusionConstants.X_TRACE_NAME)) None
      else Some(HttpUtils.generateTraceHeader())).flatten
    val request = req.copy(headers = extHeaders ++ req.headers)
    HttpUtils.curlLogging(request)(logger)
    val resultF = inner(ctx.withRequest(request))
    resultF.map {
      case RouteResult.Complete(response) =>
        val headers = extHeaders
            .find(_.name() == `X-Request-Time`.name)
            .flatMap(h => `X-Span-Time`.fromXRequestTime(h.asInstanceOf[`X-Request-Time`]))
            .toList ::: extHeaders ++ response.headers
        RouteResult.Complete(processResponse(request, response.copy(headers = headers)))
      case RouteResult.Rejected(_) => throw HSInternalErrorException("error")
    }
  }

  private def processResponse(request: HttpRequest, resp: HttpResponse): HttpResponse = {
    request.headers.find(h => h.name() == `X-Service`.name) match {
      case Some(serviceHeader) if serviceHeader.value().nonEmpty => // pre call service name
      case _                                                     => // API Gateway
    }
    val headers = core.currentXService +: resp.headers
    val response = resp.copy(headers = headers)
    HttpUtils.curlLoggingResponse(request, response, true)(logger)
  }
}
