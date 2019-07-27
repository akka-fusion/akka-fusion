package fusion.core.event.http

import java.net.InetSocketAddress

import fusion.core.event.FusionEvent

import scala.util.Try

trait HttpServerEvent extends FusionEvent

case class HttpBindingServerEvent(result: Try[InetSocketAddress], isSecure: Boolean) extends HttpServerEvent
