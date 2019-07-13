package fusion.http.filters

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.RawHeader
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.FusionConstants
import fusion.core.extension.FusionCore
import fusion.core.setting.CoreSetting
import fusion.http.AbstractHttpFilter
import fusion.http.HttpHandler

import scala.concurrent.Future

class DefaultHttpFilter(override val system: ExtendedActorSystem)
    extends AbstractHttpFilter(system)
    with StrictLogging {
  private val core        = FusionCore(system)
  private def coreSetting = core.setting

  override def execute(handler: HttpHandler): HttpHandler = { request =>
    handler(request).map(response => handleMapResponse(request, response, coreSetting))(system.dispatcher)
  }

  override def filter(request: HttpRequest): (HttpRequest, HttpResponse => Future[HttpResponse]) = {
    request.copy(headers = RawHeader("default", "default") +: request.headers) -> (resp => Future.successful(resp))
  }

  private def handleMapResponse(request: HttpRequest, response: HttpResponse, core: CoreSetting): HttpResponse = {
    val host    = System.getProperty(FusionConstants.SERVER_HOST_PATH, "127.0.0.1")
    val port    = System.getProperty(FusionConstants.SERVER_PORT_PATH, "0")
    val name    = core.name + "/" + host + ":" + port
    var headers = RawHeader(FusionConstants.HEADER_NAME, name) +: response.headers
    for (header <- request.headers if FusionConstants.TRACE_NAME == header.name()) {
      headers = header +: headers
    }
    response.copy(headers = headers)
  }

}
