package fusion.http.interceptor

import akka.http.scaladsl.server.Route

trait HttpInterceptor {

  def interceptor(inner: Route): Route

}
