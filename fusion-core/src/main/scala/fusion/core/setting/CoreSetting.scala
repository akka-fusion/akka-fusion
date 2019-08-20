package fusion.core.setting

import fusion.common.constant.FusionConstants
import helloscala.common.Configuration

class CoreSetting(configuration: Configuration) {
  def name: String = System.getProperty(FusionConstants.SERVICE_NAME_PATH, configuration.getString("fusion.name"))
}
