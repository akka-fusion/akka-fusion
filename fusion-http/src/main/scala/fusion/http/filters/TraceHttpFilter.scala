package fusion.http.filters

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.FusionConstants
import fusion.core.extension.FusionCore
import fusion.http.AbstractHttpFilter
import fusion.http.HttpHandler
import fusion.http.util.HttpUtils

import scala.concurrent.Future

class TraceHttpFilter(override val system: ExtendedActorSystem) extends AbstractHttpFilter(system) with StrictLogging {
  private val core        = FusionCore(system)
  private def coreSetting = core.setting

  override protected def execute(handler: HttpHandler): HttpHandler = { req =>
    val headers =
      if (req.headers.exists(header => header.name() == FusionConstants.TRACE_NAME)) req.headers
      else HttpUtils.generateTraceHeader(coreSetting) +: req.headers
    val request = req.copy(headers = headers)
    handler(request).map(response => handleMapResponse(request, response))(system.dispatcher)
  }

  override def filter(request: HttpRequest): (HttpRequest, HttpResponse => Future[HttpResponse]) = {
    request.copy(headers = RawHeader("trace", "trace") +: request.headers) -> (resp => Future.successful(resp))
  }

  private def handleMapResponse(request: HttpRequest, response: HttpResponse): HttpResponse = {
    if (response.headers.exists(_.name() == FusionConstants.TRACE_NAME)) {
      response
    } else {
      val headers = request.headers
        .find(header => header.name() == FusionConstants.TRACE_NAME)
        .map(traceHeader => traceHeader +: response.headers)
        .getOrElse(response.headers)
      response.copy(headers = headers)
    }
  }

}
