package fusion.discovery.client.nacos

import com.alibaba.nacos.api.config.{ConfigService => JConfigService}
import com.alibaba.nacos.api.config.listener.Listener

class ConfigService(val underling: JConfigService) {

  def getConfig(dataId: String, group: String, timeoutMs: Long): String =
    underling.getConfig(dataId, group, timeoutMs)

  def addListener(dataId: String, group: String, listener: Listener): Unit =
    underling.addListener(dataId, group, listener)

  def publishConfig(dataId: String, group: String, content: String): Boolean =
    underling.publishConfig(dataId, group, content)

  def removeConfig(dataId: String, group: String): Boolean = underling.removeConfig(dataId, group)

  def removeListener(dataId: String, group: String, listener: Listener): Unit =
    underling.removeListener(dataId, group, listener)

  def getServerStatus: String = underling.getServerStatus
}
