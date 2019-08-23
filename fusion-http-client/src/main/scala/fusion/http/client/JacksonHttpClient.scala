package fusion.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import fusion.http.util.HttpUtils

import scala.concurrent.Future
import scala.reflect.ClassTag

class JacksonHttpClient(underlying: HttpClient) extends HttpClient {
  implicit override def system: ActorSystem = underlying.system

  override def singleRequest(req: HttpRequest): Future[HttpResponse] = underlying.singleRequest(req)

  def requestToObject[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[T] = {
    import fusion.http.util.JacksonSupport._
    request(req).flatMap(response => HttpUtils.mapHttpResponse(response))(materializer.executionContext)
  }

  def requestToList[T](req: HttpRequest)(implicit ev1: ClassTag[T]): Future[List[T]] = {
    request(req).flatMap(response => HttpUtils.mapHttpResponseList(response))(materializer.executionContext)
  }

  override def close(): Unit = underlying.close()
}
