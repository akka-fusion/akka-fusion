package fusion.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer

import scala.concurrent.Future

trait HttpClient extends AutoCloseable {
  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer = ActorMaterializer()
  def singleRequest(req: HttpRequest): Future[HttpResponse]

  def request(req: HttpRequest): Future[HttpResponse] = {
    val f = singleRequest(req)
    fallback match {
      case Some(fb) => Future.firstCompletedOf(List(f, fb()))(materializer.executionContext)
      case _        => f
    }
  }
  def fallback: Option[() => Future[HttpResponse]] = None
}
