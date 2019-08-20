package akka.http

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server._
import akka.http.scaladsl.settings.ParserSettings
import akka.http.scaladsl.settings.RoutingSettings
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

object FusionRoute {

  /**
   * Turns a `Route` into an async handler function.
   */
  def asyncHandler(route: Route)(
      implicit
      routingSettings: RoutingSettings,
      parserSettings: ParserSettings,
      materializer: ActorMaterializer,
      routingLog: RoutingLog,
      executionContext: ExecutionContextExecutor = null,
      rejectionHandler: RejectionHandler = RejectionHandler.default,
      exceptionHandler: ExceptionHandler = null): HttpRequest => Future[HttpResponse] = {
    import akka.http.scaladsl.util.FastFuture._
    val effectiveEC = if (executionContext ne null) executionContext else materializer.executionContext

    {
      implicit val executionContext = effectiveEC // overrides parameter
      val effectiveParserSettings   = if (parserSettings ne null) parserSettings else ParserSettings(materializer.system)
      val sealedRoute               = route
      request =>
        sealedRoute(new RequestContextImpl(
          request,
          routingLog.requestLog(request),
          routingSettings,
          effectiveParserSettings)).fast.map {
          case RouteResult.Complete(response) => response
          case RouteResult.Rejected(rejected) =>
            throw new IllegalStateException(s"Unhandled rejections '$rejected', unsealed RejectionHandler?!")
        }
    }
  }

}
