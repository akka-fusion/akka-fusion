package fusion.http.gateway.server

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.extractRequestContext
import akka.http.scaladsl.server.Directives.onSuccess
import akka.http.scaladsl.server.Route
import fusion.discovery.client.DiscoveryHttpClient
import fusion.http.util.HttpUtils

import scala.concurrent.Future

trait GatewayDirective {

  def proxyOnServiceName(proxySetting: ProxySetting, discoveryHttpClient: DiscoveryHttpClient): Route =
    extractRequestContext { ctx =>
      implicit val ec = ctx.executionContext
      val request     = GatewayDirective.copyOnAuthorityAndSchema(ctx.request, proxySetting, discoveryHttpClient)
      val responseF   = discoveryHttpClient.hostRequest(proxySetting.requestFunc(request))
      val f =
        proxySetting.fallback.map(func => Future.firstCompletedOf(List(responseF, func(request)))).getOrElse(responseF)
      onSuccess(f) { response =>
        complete(response)
      }
    }
}

object GatewayDirective extends GatewayDirective {

  private def copyOnAuthorityAndSchema(
      request: HttpRequest,
      proxySetting: ProxySetting,
      discoveryHttpClient: DiscoveryHttpClient): HttpRequest = {
    val realRequest = HttpUtils.copyUri(request, proxySetting.scheme, proxySetting.serviceName)
    discoveryHttpClient.buildHttpRequest(realRequest)
  }
}
