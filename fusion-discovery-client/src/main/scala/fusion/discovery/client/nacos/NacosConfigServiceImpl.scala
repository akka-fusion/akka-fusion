package fusion.discovery.client.nacos

import java.util.Properties

import com.alibaba.nacos.api.config.listener.AbstractListener
import com.alibaba.nacos.api.config.{ConfigService => JConfigService}
import fusion.discovery.client.FusionConfigService

class NacosConfigServiceImpl(props: Properties, val underlying: JConfigService) extends FusionConfigService {

  override def addListener(dataId: String, group: String, listener: String => Unit): Unit =
    underlying.addListener(dataId, group, new AbstractListener {
      override def receiveConfigInfo(configInfo: String): Unit = listener(configInfo)
    })

  /**
   * Remove listener
   *
   * @param dataId   dataId
   * @param group    group
   * @param listener listener
   */
  override def removeListener(dataId: String, group: String, listener: String => Unit): Unit =
    underlying.removeListener(dataId, group, new AbstractListener {
      override def receiveConfigInfo(configInfo: String): Unit = listener(configInfo)
    })

  override def getConfig(dataId: String, group: String, timeoutMs: Long): String =
    underlying.getConfig(dataId, group, timeoutMs)

  override def getConfig: String = getConfig(props.dataId, props.group, props.timeoutMs)

  override def publishConfig(dataId: String, group: String, content: String): Boolean = {
    underlying.publishConfig(dataId, group, content)
  }

  override def removeConfig(dataId: String, group: String): Boolean = underlying.removeConfig(dataId, group)

  override def getServerStatus: String = underlying.getServerStatus
}
