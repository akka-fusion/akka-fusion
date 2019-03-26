package fusion.discovery.client

import java.util.Properties

import com.alibaba.nacos.api.naming.listener.{Event, NamingEvent}
import com.alibaba.nacos.api.naming.pojo.{Instance, ListView, ServiceInfo}
import fusion.discovery.model._
import helloscala.common.util.{AsInt, AsLong, Utils}

import scala.collection.JavaConverters._

package object nacos {
  implicit class NacosDiscoveryProperties(underlying: Properties) extends Properties {
    import fusion.core.constant.PropKeys._
    underlying.forEach((key, value) => put(key, value))

    def serviceName: Option[String]         = Utils.option(getProperty(SERVICE_NAME))
    def namespace: Option[String]           = Utils.option(getProperty(NAMESPACE))
    def dataId: String                      = getProperty(DATA_ID)
    def group: String                       = Utils.option(getProperty(GROUP)).getOrElse("DEFAULT_GROUP")
    def timeoutMs: Long                     = AsLong.unapply(get(TIMEOUT_MS)).getOrElse(3000L)
    def instanceIp: String                  = getProperty(INSTANCE_IP)
    def instancePort: Int                   = AsInt.unapply(get(INSTANCE_PORT)).get
    def instanceClusterName: Option[String] = Utils.option(getProperty(INSTANCE_CLUSTER_NAME))
    def instanceWeight: Double              = Utils.option(getProperty(INSTANCE_WEIGHT)).map(_.toDouble).getOrElse(1.0)
    def isAutoRegisterInstance: Boolean     = Option(get(AUTO_REGISTER_INSTANCE)).map(_.toString).forall(_.toBoolean)
  }

  implicit final class ToDiscoveryList[T](v: ListView[T]) {
    def toDiscoveryList: DiscoveryList[T] = DiscoveryList[T](v.getData.asScala, v.getCount)
  }

  implicit final class ToDiscoveryInstance(instance: Instance) {

    def toDiscoveryInstance: DiscoveryInstance =
      DiscoveryInstance(
        instance.getInstanceId,
        instance.getIp,
        instance.getPort,
        instance.getServiceName,
        instance.getClusterName,
        instance.getWeight,
        instance.isHealthy,
        instance.isEnabled,
        Option(instance.getMetadata).map(_.asScala.toMap).getOrElse(Map()))
  }

  implicit final class ToNacosInstantce(instance: DiscoveryInstance) {

    def toNacosInstance: Instance = {
      val payload = new Instance
      payload.setClusterName(instance.clusterName)
      payload.setEnabled(instance.enabled)
      payload.setHealthy(instance.healthy)
      payload.setInstanceId(instance.instanceId)
      payload.setIp(instance.ip)
      payload.setMetadata(instance.metadata.asJava)
      payload.setPort(instance.port)
      payload.setServiceName(instance.serviceName)
      payload.setWeight(instance.weight)
      payload
    }
  }

  implicit final class ToDiscoveryServiceInfo(v: ServiceInfo) {

    def toDiscoveryServiceInfo =
      DiscoveryServiceInfo(
        v.getName,
        v.getClusters,
        v.getCacheMillis,
        v.getHosts.asScala.map(_.toDiscoveryInstance),
        v.getLastRefTime,
        v.getChecksum,
        v.isAllIPs)
  }

  implicit final class ToDiscoveryEvent(v: Event) {

    def toDiscoveryEvent: DiscoveryEvent = v match {
      case evt: NamingEvent =>
        DiscoveryNamingEvent(evt.getServiceName, evt.getInstances.asScala.map(_.toDiscoveryInstance))
      case other => throw new IllegalArgumentException(s"Unknown Event: $other")
    }
  }
}
