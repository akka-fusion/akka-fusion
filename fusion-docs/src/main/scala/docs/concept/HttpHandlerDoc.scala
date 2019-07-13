package docs.concept

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import fusion.http.HttpHandler
import fusion.http.HttpFilter
import fusion.http.exception.HttpResponseException
import org.bson.types.ObjectId

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object HttpHandlerDoc {
  private val system = ActorSystem().asInstanceOf[ExtendedActorSystem]

  // #applyHttpInterceptorChain
  trait HttpFilter {
    def filter(handler: HttpHandler): HttpHandler
  }

  def applyHttpInterceptorChain(httpHandler: HttpHandler, filters: Iterable[HttpFilter]): HttpHandler = {
    val duce: HttpHandler = filters.foldLeft(httpHandler) {
      (h, filter) =>
//      interceptor.filter(handler)
//      val result = interceptor.filter(h)
//      req => result(req).getOrElse(h(req))
        req =>
          filter.filter(h).apply(req)
    }
    duce
  }
  // #applyHttpInterceptorChain

  def main(args: Array[String]): Unit = {
    val httpHandler: HttpHandler = req => Future.successful(HttpResponse(StatusCodes.OK, entity = "default"))
    val filters: Iterable[HttpFilter] = List(
      new HttpFilter {
        override def filter(handler: HttpHandler): HttpHandler = { req =>
          println("first interceptor")
          handler(req)
        }
      },
      new TerminationHttpFilter,
      new HttpFilter {
        override def filter(handler: HttpHandler): HttpHandler = { req =>
          println("last interceptor")
          handler(req)
        }
      })
    try {
      val handler = List.empty[HttpFilter].foldRight(httpHandler)((inter, h) => inter.filter(h))

      val responseF = handler(HttpRequest())
      val response  = Await.result(responseF, Duration.Inf)
      println("response: " + response)
    } finally {
      system.terminate()
    }
  }

  // #TerminationHttpFilter
  class TerminationHttpFilter extends HttpFilter {
    override def filter(handler: HttpHandler): HttpHandler = { req =>
      //handler(req).flatMap(resp => Future.failed(HttpResponseException(resp)))
//      handler(req).map(resp => throw HttpResponseException(resp))
      throw HttpResponseException(HttpResponse(StatusCodes.InternalServerError))
    }
  }
  // #TerminationHttpFilter

}

// #NothingHttpFilter
class NothingHttpFilter extends HttpFilter {
  override def execute(handler: HttpHandler, system: ExtendedActorSystem): HttpHandler = {
    handler
  }
}
// #NothingHttpFilter

// #TraceHttpFilter
class TraceHttpFilter extends HttpFilter {

  override def execute(handler: HttpHandler, system: ExtendedActorSystem): HttpHandler = { (req: HttpRequest) =>
    import system.dispatcher
    val traceHeader = RawHeader("trace-id", ObjectId.get().toHexString)
    val headers     = traceHeader +: req.headers
    val request     = req.copy(headers = headers)
    handler(request).map(response => toTrace(response, traceHeader))
  }

  private def toTrace(response: HttpResponse, traceHeader: RawHeader): HttpResponse = {
    val headers = traceHeader +: response.headers
    response.copy(headers = headers)
  }
}
// #TraceHttpFilter
