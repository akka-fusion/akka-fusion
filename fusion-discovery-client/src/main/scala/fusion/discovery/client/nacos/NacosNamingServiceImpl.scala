package fusion.discovery.client.nacos

import com.alibaba.nacos.api.naming.{NamingService => JNamingService}
import com.alibaba.nacos.api.selector.AbstractSelector
import com.typesafe.scalalogging.StrictLogging
import fusion.discovery.client.FusionNamingService
import fusion.discovery.model.DiscoveryEvent
import fusion.discovery.model.DiscoveryInstance
import fusion.discovery.model.DiscoveryList
import fusion.discovery.model.DiscoveryServiceInfo
import helloscala.common.exception.HSBadRequestException

import scala.collection.JavaConverters._

class NacosNamingServiceImpl(props: NacosDiscoveryProperties, val underlying: JNamingService)
    extends FusionNamingService
    with StrictLogging {

  override def registerInstance(serviceName: String, ip: String, port: Int): DiscoveryInstance =
    registerInstance(DiscoveryInstance(ip, port, serviceName))

  override def registerInstance(serviceName: String, ip: String, port: Int, clusterName: String): DiscoveryInstance =
    registerInstance(
      DiscoveryInstance(
        ip,
        port,
        serviceName,
        clusterName,
        props.instanceWeight,
        props.healthy,
        ephemeral = props.ephemeral))

  override def registerInstance(serviceName: String, instance: DiscoveryInstance): DiscoveryInstance =
    registerInstance(instance.copy(serviceName = serviceName))

  override def registerInstance(instance: DiscoveryInstance): DiscoveryInstance = {
    logger.info(s"注册服务实例到${props.namespace}: $instance")
    underlying.registerInstance(instance.serviceName, /*instance.groupName, */ instance.toNacosInstance)
    instance
  }

  override def registerInstanceCurrent(): DiscoveryInstance = {
    val inst = DiscoveryInstance(
      props.instanceIp,
      props.instancePort,
      props.serviceName.getOrElse(throw HSBadRequestException("未指定服务名 [serviceName]")),
      props.instanceClusterName,
      props.instanceWeight,
      props.healthy,
      enable = props.enable,
      ephemeral = props.ephemeral,
      group = props.group)
    registerInstance(inst)
  }

  override def deregisterInstance(serviceName: String, ip: String, port: Int): Unit = {
    underlying.deregisterInstance(serviceName, ip, port)
  }

  override def deregisterInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit = {
    underlying.deregisterInstance(serviceName, ip, port, clusterName)
  }

  override def deregisterInstance(instance: DiscoveryInstance): Unit = {
    logger.info(s"取消服务注册: $instance")
    underlying.deregisterInstance(instance.serviceName, instance.group, instance.toNacosInstance)
  }

  override def deregisterInstanceCurrent(): Unit =
    deregisterInstance(
      props.serviceName.getOrElse(throw HSBadRequestException("未指定服务名 [serviceName]")),
      props.instanceIp,
      props.instancePort,
      props.instanceClusterName)

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
