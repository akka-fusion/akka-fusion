package fusion.http.gateway.server

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Route
import fusion.discovery.client.DiscoveryHttpClient

import scala.concurrent.Future

trait GatewayRoute {
  def discoveryHttpClient: DiscoveryHttpClient

  def proxyOnServiceName(serviceName: String, fallback: Option[HttpRequest => Future[HttpResponse]]): Route =
    proxyOnServiceName(serviceName, "http", fallback)

  def proxyOnServiceName(
      serviceName: String,
      scheme: String,
      fallback: Option[HttpRequest => Future[HttpResponse]]): Route =
    proxyOnServiceName(ProxySetting(serviceName, identity, fallback, scheme))

  def proxyOnServiceName(proxySetting: ProxySetting): Route =
    GatewayDirective.proxyOnServiceName(proxySetting, discoveryHttpClient)

}
