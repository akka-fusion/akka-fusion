package fusion.discovery.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.Uri
import akka.stream.ActorMaterializer
import fusion.http.client.HttpClient
import helloscala.common.Configuration

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class DiscoveryHttpClient(
    val namingService: FusionNamingService,
    val materializer: ActorMaterializer,
    val clientConfiguration: Configuration)
    extends HttpClient {
  implicit protected def mat: ActorMaterializer = materializer
  implicit protected def system: ActorSystem    = materializer.system

  def buildUri(uri: Uri): Uri
  def buildHttpRequest(req: HttpRequest): HttpRequest = req.withUri(buildUri(req.uri))

  def hostRequest(req: HttpRequest): Future[HttpResponse]
  def hostRequestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T]
  def hostRequestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]]
  override def request(req: HttpRequest): Future[HttpResponse] = {
    val f = hostRequest(req)
    fallback match {
      case Some(fb) => Future.firstCompletedOf(List(f, fb()))(materializer.executionContext)
      case _        => f
    }
  }
  override def requestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T]     = hostRequestToObject(req)
  override def requestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = hostRequestToList(req)
  override def singleRequest(req: HttpRequest): Future[HttpResponse] =
    Http()(system).singleRequest(buildHttpRequest(req))
}
