package fusion.metric.http

import akka.http.scaladsl.server.Route
import fusion.core.constant.FusionConstant
import fusion.http.server.AbstractRoute
import helloscala.common.Configuration

class MetricRoute(configuration: Configuration) extends AbstractRoute {
  private val conf = configuration.getConfiguration(s"${FusionConstant.ROOT_PREFIX}metric.http")

  override def route: Route = pathPrefix(conf.getString("route-context")) {
    healthRoute
  }

  def healthRoute: Route = pathGet("health") {
    completeOk
  }

}
