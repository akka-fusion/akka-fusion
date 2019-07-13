package fusion.http.server

import akka.http.scaladsl.server.RejectionHandler

trait FusionRejectionHandler {
  val rejectionHandler: RejectionHandler
}
