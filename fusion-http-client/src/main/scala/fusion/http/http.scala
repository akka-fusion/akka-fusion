package fusion

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.SourceQueueWithComplete

import scala.concurrent.Future
import scala.concurrent.Promise

package object http {
  type HttpHandler            = HttpRequest => Future[HttpResponse]
  type HttpFilterHandler      = HttpRequest => Future[Option[HttpResponse]]
  type SourceQueueElementType = (HttpRequest, Promise[HttpResponse])
  type HttpSourceQueue        = SourceQueueWithComplete[SourceQueueElementType]
}
