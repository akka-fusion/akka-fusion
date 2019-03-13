package fusion.discovery.client.nacos

import com.alibaba.nacos.api.naming.{NamingService => JNamingService}
import com.alibaba.nacos.api.naming.listener.EventListener
import com.alibaba.nacos.api.naming.pojo.{Instance, ListView, ServiceInfo}
import com.alibaba.nacos.api.selector.AbstractSelector

import scala.collection.JavaConverters._

class NamingService(val underling: JNamingService) {

  def registerInstance(serviceName: String, ip: String, port: Int): Unit =
    underling.registerInstance(serviceName, ip, port)

  def registerInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit =
    underling.registerInstance(serviceName, ip, port, clusterName)

  def registerInstance(serviceName: String, instance: Instance): Unit =
    underling.registerInstance(serviceName, instance)

  def deregisterInstance(serviceName: String, ip: String, port: Int): Unit =
    underling.deregisterInstance(serviceName, ip, port)

  def deregisterInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit =
    underling.deregisterInstance(serviceName, ip, port, clusterName)

  def getAllInstances(serviceName: String): Seq[Instance] =
    underling.getAllInstances(serviceName).asScala

  def getAllInstances(serviceName: String, subscribe: Boolean): Seq[Instance] =
    underling.getAllInstances(serviceName, subscribe).asScala

  def getAllInstances(serviceName: String, clusters: Seq[String]): Seq[Instance] =
    underling.getAllInstances(serviceName, clusters.asJava).asScala

  def getAllInstances(serviceName: String, clusters: Seq[String], subscribe: Boolean): Seq[Instance] =
    underling.getAllInstances(serviceName, clusters.asJava, subscribe).asScala

  def selectInstances(serviceName: String, healthy: Boolean): Seq[Instance] =
    underling.selectInstances(serviceName, healthy).asScala

  def selectInstances(serviceName: String, healthy: Boolean, subscribe: Boolean): Seq[Instance] =
    underling.selectInstances(serviceName, healthy, subscribe).asScala

  def selectInstances(serviceName: String, clusters: Seq[String], healthy: Boolean): Seq[Instance] =
    underling.selectInstances(serviceName, clusters.asJava, healthy).asScala

  def selectInstances(serviceName: String, clusters: Seq[String], healthy: Boolean, subscribe: Boolean): Seq[Instance] =
    underling.selectInstances(serviceName, clusters.asJava, healthy, subscribe).asScala

  def selectOneHealthyInstance(serviceName: String): Instance = underling.selectOneHealthyInstance(serviceName)

  def selectOneHealthyInstance(serviceName: String, subscribe: Boolean): Instance =
    underling.selectOneHealthyInstance(serviceName, subscribe)

  def selectOneHealthyInstance(serviceName: String, clusters: Seq[String]): Instance =
    underling.selectOneHealthyInstance(serviceName, clusters.asJava)

  def selectOneHealthyInstance(serviceName: String, clusters: Seq[String], subscribe: Boolean): Instance =
    underling.selectOneHealthyInstance(serviceName, clusters.asJava, subscribe)

  def subscribe(serviceName: String, listener: EventListener): Unit =
    underling.subscribe(serviceName, listener)

  def subscribe(serviceName: String, clusters: Seq[String], listener: EventListener): Unit =
    underling.subscribe(serviceName, clusters.asJava, listener)

  def unsubscribe(serviceName: String, listener: EventListener): Unit =
    underling.unsubscribe(serviceName, listener)

  def unsubscribe(serviceName: String, clusters: Seq[String], listener: EventListener): Unit =
    underling.unsubscribe(serviceName, clusters.asJava, listener)

  def getServicesOfServer(pageNo: Int, pageSize: Int): ListView[String] =
    underling.getServicesOfServer(pageNo, pageSize)

  def getServicesOfServer(pageNo: Int, pageSize: Int, selector: AbstractSelector): ListView[String] =
    underling.getServicesOfServer(pageNo, pageSize, selector)

  def getSubscribeServices: Seq[ServiceInfo] = underling.getSubscribeServices.asScala

  def getServerStatus: String = underling.getServerStatus
}
