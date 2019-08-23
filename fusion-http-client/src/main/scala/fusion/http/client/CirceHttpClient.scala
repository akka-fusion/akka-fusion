package fusion.http.client

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse

import scala.concurrent.Future

class CirceHttpClient(underlying: HttpClient) extends HttpClient {
  implicit override def system: ActorSystem = underlying.system

  override def singleRequest(req: HttpRequest): Future[HttpResponse] = underlying.singleRequest(req)

  override def close(): Unit = underlying.close()
}
