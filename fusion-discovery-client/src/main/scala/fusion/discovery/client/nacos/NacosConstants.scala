package fusion.discovery.client.nacos

import fusion.discovery.DiscoveryConstants

object NacosConstants {
  import fusion.core.constant.PropKeys._
  val NAME = "nacos"
  val CONF_PATH = s"${DiscoveryConstants.CONF_PATH}.$NAME"
  val CONF_SERVER_ADDR = s"$CONF_PATH.$SERVER_ADDR"
  val CONF_NAMESPACE = s"$CONF_PATH.$NAMESPACE"
  val CONF_DATA_ID = s"$CONF_PATH.$DATA_ID"
  val CONF_TIMEOUT_MS = s"$CONF_PATH.$TIMEOUT_MS"
  val CONF_SERVICE_NAME = s"$CONF_PATH.$SERVICE_NAME"
}
