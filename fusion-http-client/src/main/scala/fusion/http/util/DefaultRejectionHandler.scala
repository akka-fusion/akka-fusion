package fusion.http.util

import akka.http.scaladsl.server.Rejection
import akka.http.scaladsl.server.RejectionHandler
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging

import scala.collection.immutable

final class DefaultRejectionHandler extends RejectionHandler with StrictLogging {
  override def apply(rejections: immutable.Seq[Rejection]): Option[Route] = {
    BaseRejectionBuilder.rejectionHandler(rejections)
  }
}
