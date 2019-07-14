package fusion.core.event.http

import akka.http.scaladsl.Http.ServerBinding
import fusion.core.event.FusionEvent

import scala.util.Try

trait HttpServerEvent extends FusionEvent

case class HttpBindingServerEvent(result: Try[ServerBinding], isSecure: Boolean) extends HttpServerEvent
