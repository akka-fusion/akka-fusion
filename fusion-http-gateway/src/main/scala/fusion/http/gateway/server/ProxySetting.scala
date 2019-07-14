package fusion.http.gateway.server

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri

import scala.concurrent.Future

final class ProxySetting private (
    val serviceName: String,
    val requestFunc: HttpRequest => HttpRequest,
    val fallback: Option[HttpRequest => Future[HttpResponse]],
    val scheme: String)

object ProxySetting {

  def apply(serviceName: String, fallback: HttpRequest => Future[HttpResponse]): ProxySetting =
    apply(serviceName, identity, Some(fallback), "http")

  def apply(serviceName: String, uri: Uri, fallback: Option[HttpRequest => Future[HttpResponse]]): ProxySetting =
    apply(serviceName, request => request.copy(uri = uri), fallback, "http")

  def apply(
      serviceName: String,
      requestFunc: HttpRequest => HttpRequest,
      fallback: HttpRequest => Future[HttpResponse]): ProxySetting =
    apply(serviceName, requestFunc, Some(fallback), "http")

  def apply(
      serviceName: String,
      requestFunc: HttpRequest => HttpRequest,
      fallback: Option[HttpRequest => Future[HttpResponse]],
      scheme: String): ProxySetting = new ProxySetting(serviceName, requestFunc, fallback, scheme)
}
