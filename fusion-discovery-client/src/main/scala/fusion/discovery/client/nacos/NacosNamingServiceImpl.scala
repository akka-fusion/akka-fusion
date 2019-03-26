package fusion.discovery.client.nacos

import com.alibaba.nacos.api.common.Constants
import com.alibaba.nacos.api.naming.pojo.Instance
import com.alibaba.nacos.api.naming.{NamingService => JNamingService}
import com.alibaba.nacos.api.selector.AbstractSelector
import com.typesafe.config.Config
import fusion.discovery.client.FusionNamingService
import fusion.discovery.model.{DiscoveryEvent, DiscoveryInstance, DiscoveryList, DiscoveryServiceInfo}
import helloscala.common.Configuration

import scala.collection.JavaConverters._

class NacosNamingServiceImpl(props: NacosDiscoveryProperties, val underlying: JNamingService)
    extends FusionNamingService {

  override def registerInstance(serviceName: String, ip: String, port: Int): Unit =
    underlying.registerInstance(serviceName, ip, port)

  override def registerInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit =
    underlying.registerInstance(serviceName, ip, port, clusterName)

  override def registerInstance(serviceName: String, instance: DiscoveryInstance): Unit =
    underlying.registerInstance(serviceName, instance.toNacosInstance)

  override def registerInstanceCurrent(config: Config): Unit = {
    val instance: Instance = new Instance
    instance.setIp(props.instanceIp)
    instance.setPort(props.instancePort)
    instance.setWeight(props.instanceWeight)
    props.instanceClusterName.foreach(instance.setClusterName)
    registerInstance(props.serviceName.getOrElse(config.getString("fusion.name")), instance.toDiscoveryInstance)
  }

  override def deregisterInstance(serviceName: String, ip: String, port: Int): Unit =
    underlying.deregisterInstance(serviceName, ip, port)

  override def deregisterInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit =
    underlying.deregisterInstance(serviceName, ip, port, clusterName)

  override def deregisterInstanceCurrent(config: Config): Unit =
    deregisterInstance(
      props.serviceName.getOrElse(config.getString("fusion.name")),
      props.instanceIp,
      props.instancePort,
      props.instanceClusterName.getOrElse(Constants.NAMING_DEFAULT_CLUSTER_NAME))

  override def getAllInstances(serviceName: String): Seq[DiscoveryInstance] =
    underlying.getAllInstances(serviceName).asScala.map(_.toDiscoveryInstance)

  override def getAllInstances(serviceName: String, subscribe: Boolean): Seq[DiscoveryInstance] =
    underlying.getAllInstances(serviceName, subscribe).asScala.map(_.toDiscoveryInstance)

  def getAllInstances(serviceName: String, clusters: Seq[String]): Seq[DiscoveryInstance] =
    underlying.getAllInstances(serviceName, clusters.asJava).asScala.map(_.toDiscoveryInstance)

  def getAllInstances(serviceName: String, clusters: Seq[String], subscribe: Boolean): Seq[DiscoveryInstance] =
    underlying.getAllInstances(serviceName, clusters.asJava, subscribe).asScala.map(_.toDiscoveryInstance)

  override def selectInstances(serviceName: String, healthy: Boolean): Seq[DiscoveryInstance] =
    underlying.selectInstances(serviceName, healthy).asScala.map(_.toDiscoveryInstance)

  override def selectInstances(serviceName: String, healthy: Boolean, subscribe: Boolean): Seq[DiscoveryInstance] =
    underlying.selectInstances(serviceName, healthy, subscribe).asScala.map(_.toDiscoveryInstance)

  def selectInstances(serviceName: String, clusters: Seq[String], healthy: Boolean): Seq[DiscoveryInstance] =
    underlying.selectInstances(serviceName, clusters.asJava, healthy).asScala.map(_.toDiscoveryInstance)

  def selectInstances(
      serviceName: String,
      clusters: Seq[String],
      healthy: Boolean,
      subscribe: Boolean): Seq[DiscoveryInstance] =
    underlying.selectInstances(serviceName, clusters.asJava, healthy, subscribe).asScala.map(_.toDiscoveryInstance)

  override def selectOneHealthyInstance(serviceName: String): DiscoveryInstance =
    underlying.selectOneHealthyInstance(serviceName).toDiscoveryInstance

  override def selectOneHealthyInstance(serviceName: String, subscribe: Boolean): DiscoveryInstance =
    underlying.selectOneHealthyInstance(serviceName, subscribe).toDiscoveryInstance

  def selectOneHealthyInstance(serviceName: String, clusters: Seq[String]): DiscoveryInstance =
    underlying.selectOneHealthyInstance(serviceName, clusters.asJava).toDiscoveryInstance

  def selectOneHealthyInstance(serviceName: String, clusters: Seq[String], subscribe: Boolean): DiscoveryInstance =
    underlying.selectOneHealthyInstance(serviceName, clusters.asJava, subscribe).toDiscoveryInstance

  override def subscribe(serviceName: String, listener: DiscoveryEvent => Unit): Unit =
    underlying.subscribe(serviceName, evt => listener(evt.toDiscoveryEvent))

  def subscribe(serviceName: String, clusters: Seq[String], listener: DiscoveryEvent => Unit): Unit =
    underlying.subscribe(serviceName, clusters.asJava, evt => listener(evt.toDiscoveryEvent))

  def unsubscribe(serviceName: String, listener: DiscoveryEvent => Unit): Unit =
    underlying.unsubscribe(serviceName, evt => listener(evt.toDiscoveryEvent))

  def unsubscribe(serviceName: String, clusters: Seq[String], listener: DiscoveryEvent => Unit): Unit =
    underlying.unsubscribe(serviceName, clusters.asJava, evt => listener(evt.toDiscoveryEvent))

  def getServicesOfServer(pageNo: Int, pageSize: Int): DiscoveryList[String] =
    underlying.getServicesOfServer(pageNo, pageSize).toDiscoveryList

  def getServicesOfServer(pageNo: Int, pageSize: Int, selector: AbstractSelector): DiscoveryList[String] =
    underlying.getServicesOfServer(pageNo, pageSize, selector).toDiscoveryList

  def getSubscribeServices: Seq[DiscoveryServiceInfo] =
    underlying.getSubscribeServices.asScala.map(_.toDiscoveryServiceInfo)

  def getServerStatus: String = underlying.getServerStatus
}
