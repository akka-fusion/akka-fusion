package fusion.actuator.setting

import fusion.actuator.constant.ActuatorConstants
import helloscala.common.Configuration

case class ActuatorSetting(configuration: Configuration) {
  private val c           = configuration.getConfig(ActuatorConstants.PATH_ROOT)
  def contextPath: String = c.getString("context-path")
}
