package fusion.discovery.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import fusion.http.util.HttpUtils

import scala.concurrent.Future
import scala.reflect.ClassTag

class JacksonDiscoveryHttpClient(underlying: DiscoveryHttpClient) extends DiscoveryHttpClient {
  override val clientSetting: DiscoveryHttpClientSetting = underlying.clientSetting
  implicit override val system: ActorSystem              = underlying.system

  def hostRequestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T] = {
    import fusion.http.util.JacksonSupport._
    hostRequest(req).flatMap(response => HttpUtils.mapHttpResponse(response))(materializer.executionContext)
  }

  def hostRequestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = {
    hostRequest(req).flatMap(response => HttpUtils.mapHttpResponseList(response))(materializer.executionContext)
  }

  def requestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T] = {
    import fusion.http.util.JacksonSupport._
    request(req).flatMap(response => HttpUtils.mapHttpResponse(response))(materializer.executionContext)
  }

  def requestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = {
    request(req).flatMap(response => HttpUtils.mapHttpResponseList(response))(materializer.executionContext)
  }

  override def buildUri(uri: Uri): Future[Uri] = underlying.buildUri(uri)
}
