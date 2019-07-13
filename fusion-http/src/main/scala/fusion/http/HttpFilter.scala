package fusion.http

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

trait HttpFilter {
  def execute(handler: HttpHandler, system: ExtendedActorSystem): HttpHandler

  def filter(request: HttpRequest): (HttpRequest, HttpResponse => Future[HttpResponse]) = {
    (request, response => Future.successful(response))
  }

  def filterRequest(request: HttpRequest): HttpRequest = request

  def filterResponse(response: HttpResponse): Future[HttpResponse] = {
    Future.successful(response)
  }
}

abstract class AbstractHttpFilter(val system: ExtendedActorSystem) extends HttpFilter {
  implicit protected def ec: ExecutionContextExecutor = system.dispatcher

  final override def execute(handler: HttpHandler, system: ExtendedActorSystem): HttpHandler = {
    this.execute(handler)
  }

  protected def execute(handler: HttpHandler): HttpHandler
}
