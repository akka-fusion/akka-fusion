package fusion.http.filters

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.scalalogging.StrictLogging
import fusion.core.extension.FusionCore
import fusion.core.setting.CoreSetting
import fusion.http.util.HttpUtils
import fusion.http.AbstractHttpFilter
import fusion.http.HttpHandler

import scala.concurrent.Future

class CurlLogHttpFilter(override val system: ExtendedActorSystem)
    extends AbstractHttpFilter(system)
    with StrictLogging {
  private val core        = FusionCore(system)
  private def coreSetting = core.setting

  override def execute(handler: HttpHandler): HttpHandler = { request =>
    HttpUtils.curlLogging(request)(logger)
    val result = handler(request)
    result.map(response => handleMapResponse(request, response, coreSetting))(system.dispatcher)
  }

  override def filter(request: HttpRequest): (HttpRequest, HttpResponse => Future[HttpResponse]) = {
    HttpUtils.curlLogging(request)(logger)
    request.copy(headers = RawHeader("curl", "curl") +: request.headers) -> (resp =>
      Future {
        HttpUtils.curlLoggingResponse(request, resp)(logger)
        resp
      })
  }

  private def handleMapResponse(request: HttpRequest, response: HttpResponse, core: CoreSetting): HttpResponse = {
    HttpUtils.curlLoggingResponse(request, response)(logger)
    response
  }

}
