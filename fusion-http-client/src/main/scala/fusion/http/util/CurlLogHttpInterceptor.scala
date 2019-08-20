//package fusion.http.util
//
//import akka.actor.ExtendedActorSystem
//import akka.http.scaladsl.model.HttpResponse
//import akka.http.scaladsl.model.StatusCodes
//import akka.http.scaladsl.server.RequestContext
//import akka.http.scaladsl.server.Route
//import akka.http.scaladsl.server.RouteResult
//import com.typesafe.scalalogging.StrictLogging
//import fusion.http.interceptor.HttpInterceptor
//
//final class CurlLogHttpInterceptor(system: ExtendedActorSystem) extends HttpInterceptor with StrictLogging {
//  import system.dispatcher
//
//  override def interceptor(inner: Route): Route = { ctx =>
//    val request = ctx.request
//    HttpUtils.curlLogging(request)(logger)
//    val route = inner(ctx)
//    route.foreach(handleMapResponse(ctx, _))
//    route
//  }
//
//  private def handleMapResponse(ctx: RequestContext, route: RouteResult): Unit = route match {
//    case RouteResult.Complete(response) => HttpUtils.curlLoggingResponse(ctx.request, response)(logger)
//    case RouteResult.Rejected(rejections) =>
//      HttpUtils.curlLoggingResponse(ctx.request, HttpResponse(StatusCodes.InternalServerError))(logger) // TODO
//  }
//
//}
