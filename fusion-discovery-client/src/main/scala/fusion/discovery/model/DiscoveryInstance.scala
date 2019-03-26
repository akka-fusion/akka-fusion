package fusion.discovery.model

import helloscala.common.jackson.Jackson

case class DiscoveryInstance(
    instanceId: String,
    ip: String,
    port: Int,
    serviceName: String,
    clusterName: String = "",
    weight: Double = 1.0D,
    healthy: Boolean = true,
    enabled: Boolean = true,
    metadata: Map[String, String] = Map()) {

  def toInetAddr: String = ip + ":" + port

  override def toString: String = Jackson.stringify(this)
}
