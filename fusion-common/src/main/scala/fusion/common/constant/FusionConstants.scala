package fusion.common.constant

object FusionConstants {
  val X_SERVER             = "X-Server"
  val X_TRACE_NAME         = "X-Trace-Id"
  val X_REQUEST_TIME       = "X-Request-Time"
  val X_SPAN_TIME          = "X-Span-Time"
  val HEADER_NAME          = "Fusion-Server"
  val NAME                 = "fusion"
  val CONF_PATH            = "fusion"
  val NAME_PATH            = s"$CONF_PATH.name"
  val SERVER_HOST_PATH     = s"$CONF_PATH.server.host"
  val SERVER_PORT_PATH     = s"$CONF_PATH.server.port"
  val SERVICE_NAME_PATH    = s"$CONF_PATH.service.name"
  val PROFILES_ACTIVE_PATH = s"$CONF_PATH.profiles.active"
}
