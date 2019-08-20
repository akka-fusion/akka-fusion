package fusion.http.util

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import fusion.common.constant.FusionConstants
import fusion.core.extension.FusionCore
import fusion.core.http.headers.`X-Request-Time`
import fusion.core.http.headers.`X-Span-Time`
import fusion.core.http.headers.`X-Service`
import fusion.http.interceptor.HttpInterceptor
import helloscala.common.exception.HSInternalErrorException
import com.typesafe.scalalogging.StrictLogging

final class DefaultHttpInterceptor(system: ExtendedActorSystem) extends HttpInterceptor with StrictLogging {
  import system.dispatcher
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

    inner(ctx.withRequest(request)).map {
      case RouteResult.Complete(response) =>
        val headers = extHeaders
            .find(_.name() == `X-Request-Time`.name)
            .map(h => `X-Span-Time`.fromXRequestTime(h.asInstanceOf[`X-Request-Time`]))
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
    val headers  = core.currentXService +: resp.headers
    val response = resp.copy(headers = headers)
    HttpUtils.curlLoggingResponse(request, response)(logger)
  }
}
