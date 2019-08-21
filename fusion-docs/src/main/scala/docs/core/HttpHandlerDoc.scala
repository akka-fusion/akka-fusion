package docs.core

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteResult
import fusion.http.HttpHandler
import fusion.http.exception.HttpResponseException
import fusion.http.interceptor.HttpInterceptor
import org.bson.types.ObjectId

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

object HttpHandlerDoc {
  private val system = ActorSystem().asInstanceOf[ExtendedActorSystem]

  // #applyHttpInterceptorChain
  trait HttpInterceptor {
    def filter(handler: HttpHandler): HttpHandler
  }

  def applyHttpInterceptorChain(httpHandler: HttpHandler, filters: Iterable[HttpInterceptor]): HttpHandler = {
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
    val filters: Iterable[HttpInterceptor] = List(
      new HttpInterceptor {
        override def filter(handler: HttpHandler): HttpHandler = { req =>
          println("first interceptor")
          handler(req)
        }
      },
      new TerminationHttpInterceptor,
      new HttpInterceptor {
        override def filter(handler: HttpHandler): HttpHandler = { req =>
          println("last interceptor")
          handler(req)
        }
      })
    try {
      val handler = List.empty[HttpInterceptor].foldRight(httpHandler)((inter, h) => inter.filter(h))

      val responseF = handler(HttpRequest())
      val response  = Await.result(responseF, Duration.Inf)
      println("response: " + response)
    } finally {
      system.terminate()
    }
  }

  // #TerminationHttpInterceptor
  class TerminationHttpInterceptor extends HttpInterceptor {
    override def filter(handler: HttpHandler): HttpHandler = { req =>
      //handler(req).flatMap(resp => Future.failed(HttpResponseException(resp)))
//      handler(req).map(resp => throw HttpResponseException(resp))
      throw HttpResponseException(HttpResponse(StatusCodes.InternalServerError))
    }
  }
  // #TerminationHttpInterceptor

}

// #NothingHttpInterceptor
class NothingHttpInterceptor extends HttpInterceptor {
  override def interceptor(route: Route): Route = { ctx =>
    route(ctx)
  }
}
// #NothingHttpInterceptor

// #TraceHttpInterceptor
class TraceHttpInterceptor(system: ActorSystem) extends HttpInterceptor {
  import system.dispatcher

  override def interceptor(route: Route): Route = { ctx =>
    val req         = ctx.request
    val traceHeader = RawHeader("trace-id", ObjectId.get().toHexString)
    val headers     = traceHeader +: req.headers
    val request     = req.copy(headers = headers)
    route(ctx.withRequest(request)).map {
      case RouteResult.Complete(response) => RouteResult.Complete(toTrace(response, traceHeader))
      case a @ RouteResult.Rejected(_)    => a
    }
  }

  private def toTrace(response: HttpResponse, traceHeader: RawHeader): HttpResponse = {
    val headers = traceHeader +: response.headers
    response.copy(headers = headers)
  }
}
// #TraceHttpInterceptor
