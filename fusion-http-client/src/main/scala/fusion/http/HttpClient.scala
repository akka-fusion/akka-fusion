package fusion.http

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.reflect.ClassTag

trait HttpClient extends AutoCloseable {
  val materializer: ActorMaterializer
  def singleRequest(req: HttpRequest): Future[HttpResponse]
  def requestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T]
  def requestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]]

  def request(req: HttpRequest): Future[HttpResponse] = {
    val f = singleRequest(req)
    fallback match {
      case Some(fb) => Future.firstCompletedOf(List(f, fb()))(materializer.executionContext)
      case _        => f
    }
  }
  def fallback: Option[() => Future[HttpResponse]] = None
}
