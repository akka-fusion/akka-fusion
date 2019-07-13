package fusion.discovery.client

import java.util.Properties
import java.util.function.BiConsumer

import com.alibaba.nacos.api.common.Constants
import com.alibaba.nacos.api.naming.listener.Event
import com.alibaba.nacos.api.naming.listener.NamingEvent
import com.alibaba.nacos.api.naming.pojo.Instance
import com.alibaba.nacos.api.naming.pojo.ListView
import com.alibaba.nacos.api.naming.pojo.ServiceInfo
import com.typesafe.scalalogging.StrictLogging
import fusion.common.constant.FusionConstants
import fusion.discovery.model._
import helloscala.common.util.AsBoolean
import helloscala.common.util.AsInt
import helloscala.common.util.AsLong
import helloscala.common.util.Utils

import scala.collection.JavaConverters._

package object nacos {
  implicit final class NacosDiscoveryProperties(underlying: Properties) extends Properties with StrictLogging {
    import fusion.common.constant.PropKeys._
    underlying.forEach(new BiConsumer[AnyRef, AnyRef] {
      override def accept(t: AnyRef, u: AnyRef): Unit = put(t, u)
    })

    def serviceName: Option[String] = Utils.option(getProperty(SERVICE_NAME))
    def namespace: Option[String]   = Utils.option(getProperty(NAMESPACE))
    def dataId: String              = getProperty(DATA_ID)
    def group: String               = Utils.option(getProperty(GROUP)).getOrElse(NacosConstants.DEFAULT_GROUP)
    def timeoutMs: Long             = AsLong.unapply(get(TIMEOUT_MS)).getOrElse(3000L)

    def instanceIp: String = {
      val ip = System.getProperty(FusionConstants.SERVER_HOST_PATH)
      Utils.option(ip).getOrElse(getProperty(INSTANCE_IP))
    }

    def instancePort: Int = {
      AsInt
        .unapply(Option(System.getProperty(FusionConstants.SERVER_PORT_PATH)).getOrElse(get(INSTANCE_PORT)))
        .getOrElse(throw new ExceptionInInitializerError("instance port 未设置"))
    }

    def instanceClusterName: String = Utils.option(getProperty(CLUSTER_NAME)).getOrElse(Constants.DEFAULT_CLUSTER_NAME)
    def instanceWeight: Double      = Utils.option(getProperty(INSTANCE_WEIGHT)).map(_.toDouble).getOrElse(1.0)
    def healthy: Boolean            = Utils.option(getProperty(HEALTHY)).forall(_.toBoolean)
    def ephemeral: Boolean          = Utils.option(getProperty(EPHEMERAL)).forall(_.toBoolean)
    def enable: Boolean             = Utils.option(getProperty(ENABLE)).forall(_.toBoolean)

    def isAutoRegisterInstance: Boolean =
      Option(get(AUTO_REGISTER_INSTANCE))
        .flatMap(v => AsBoolean.unapply(v))
        .getOrElse(NacosConstants.DEFAULT_AUTO_REGISTER_INSTANCE)
  }

  implicit final class ToDiscoveryList[T](v: ListView[T]) {
    def toDiscoveryList: DiscoveryList[T] = DiscoveryList[T](v.getData.asScala, v.getCount)
  }

  implicit final class ToDiscoveryInstance(instance: Instance) {

    def toDiscoveryInstance: DiscoveryInstance =
      DiscoveryInstance(
        instance.getIp,
        instance.getPort,
        instance.getServiceName,
        instance.getClusterName,
        instance.getWeight,
        instance.isHealthy,
        instance.isEnabled,
        instance.isEphemeral,
        Option(instance.getMetadata).map(_.asScala.toMap).getOrElse(Map()),
        Constants.DEFAULT_GROUP,
        instance.getInstanceId)
  }

  implicit final class ToNacosInstantce(instance: DiscoveryInstance) {

    def toNacosInstance: Instance = {
      val payload = new Instance
      payload.setClusterName(instance.clusterName)
      payload.setEnabled(instance.enable)
      payload.setHealthy(instance.healthy)
      payload.setInstanceId(instance.instanceId)
      payload.setIp(instance.ip)
      payload.setMetadata(instance.metadata.asJava)
      payload.setPort(instance.port)
      payload.setServiceName(instance.serviceName)
      payload.setWeight(instance.weight)
      payload.setEphemeral(instance.ephemeral)
      payload.setMetadata(instance.metadata.asJava)
      payload
    }
  }

  implicit final class ToDiscoveryServiceInfo(v: ServiceInfo) {

    def toDiscoveryServiceInfo =
      DiscoveryServiceInfo(
        v.getName,
        v.getGroupName,
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
