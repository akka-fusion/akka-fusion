package fusion.discovery.model

import com.alibaba.nacos.api.common.Constants
import helloscala.common.jackson.Jackson

case class DiscoveryInstance(
    ip: String,
    port: Int,
    serviceName: String,
    clusterName: String = Constants.DEFAULT_CLUSTER_NAME,
    weight: Double = 1.0D,
    healthy: Boolean = true,
    enabled: Boolean = true,
    ephemeral: Boolean = true,
    metadata: Map[String, String] = Map(),
    group: String = Constants.DEFAULT_GROUP,
    // ip#port#clasterName#group@@serviceName
    instanceId: String = null) {

  def toInetAddr: String = ip + ":" + port

  override def toString: String = Jackson.stringify(this)
}
