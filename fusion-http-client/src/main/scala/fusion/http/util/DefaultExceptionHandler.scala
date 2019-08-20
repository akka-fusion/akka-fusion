package fusion.http.util

import akka.http.scaladsl.server.ExceptionHandler
import akka.http.scaladsl.server.Route

class DefaultExceptionHandler extends ExceptionHandler.PF {
  private val pf = BaseExceptionHandler.exceptionHandlerPF

  override def isDefinedAt(t: Throwable): Boolean = pf.isDefinedAt(t)

  override def apply(t: Throwable): Route = pf(t)
}
