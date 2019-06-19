package fusion.core.constant

object FusionConstants {
  val HEADER_NAME           = "Fusion-Server"
  val NAME                  = "fusion"
  val CONF_PATH             = "fusion"
  val NAME_PATH             = s"$CONF_PATH.name"
  val ROOT_PREFIX: String   = CONF_PATH + "."
  val SERVER_HOST_PATH      = s"${FusionConstants.CONF_PATH}.server.host"
  val SERVER_PORT_PATH      = s"${FusionConstants.CONF_PATH}.server.port"
  val APPLICATION_NAME_PATH = s"${FusionConstants.CONF_PATH}.application.name"
  val PROFILES_ACTIVE_PATH  = s"${FusionConstants.CONF_PATH}.profiles.active"
}
