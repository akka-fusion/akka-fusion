package fusion.discovery.client

import com.typesafe.config.Config
import fusion.discovery.model.{DiscoveryEvent, DiscoveryInstance, DiscoveryList, DiscoveryServiceInfo}

trait FusionNamingService {

  /**
   * register a instance to service
   *
   * @param serviceName name of service
   * @param ip          instance ip
   * @param port        instance port
   */
  def registerInstance(serviceName: String, ip: String, port: Int): Unit

  /**
   * register a instance to service with specified cluster name
   *
   * @param serviceName name of service
   * @param ip          instance ip
   * @param port        instance port
   * @param clusterName instance cluster name
   *
   */
  def registerInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit

  /**
   * register a instance to service with specified instance properties
   *
   * @param serviceName name of service
   * @param instance    instance to register
   *
   */
  def registerInstance(serviceName: String, instance: DiscoveryInstance): Unit

  def registerInstanceCurrent(config: Config): Unit

  /**
   * deregister instance from a service
   *
   * @param serviceName name of service
   * @param ip          instance ip
   * @param port        instance port
   *
   */
  def deregisterInstance(serviceName: String, ip: String, port: Int): Unit

  /**
   * deregister instance with specified cluster name from a service
   *
   * @param serviceName name of service
   * @param ip          instance ip
   * @param port        instance port
   * @param clusterName instance cluster name
   *
   */
  def deregisterInstance(serviceName: String, ip: String, port: Int, clusterName: String): Unit

  def deregisterInstanceCurrent(config: Config): Unit

  /**
   * get all instances of a service
   *
   * @param serviceName name of service
   * @return A list of instance
   *
   */
  def getAllInstances(serviceName: String): Seq[DiscoveryInstance]

  /**
   * Get all instances of a service
   *
   * @param serviceName name of service
   * @param subscribe   if subscribe the service
   * @return A list of instance
   *
   */
  def getAllInstances(serviceName: String, subscribe: Boolean): Seq[DiscoveryInstance]

  /**
   * Get all instances within specified clusters of a service
   *
   * @param serviceName name of service
   * @param clusters    list of cluster
   * @return A list of qualified instance
   *
   */
  def getAllInstances(serviceName: String, clusters: Seq[String]): Seq[DiscoveryInstance]

  /**
   * Get all instances within specified clusters of a service
   *
   * @param serviceName name of service
   * @param clusters    list of cluster
   * @param subscribe   if subscribe the service
   * @return A list of qualified instance
   *
   */
  def getAllInstances(serviceName: String, clusters: Seq[String], subscribe: Boolean): Seq[DiscoveryInstance]

  /**
   * Get qualified instances of service
   *
   * @param serviceName name of service
   * @param healthy     a flag to indicate returning healthy or unhealthy instances
   * @return A qualified list of instance
   *
   */
  def selectInstances(serviceName: String, healthy: Boolean): Seq[DiscoveryInstance]

  /**
   * Get qualified instances of service
   *
   * @param serviceName name of service
   * @param healthy     a flag to indicate returning healthy or unhealthy instances
   * @param subscribe   if subscribe the service
   * @return A qualified list of instance
   *
   */
  def selectInstances(serviceName: String, healthy: Boolean, subscribe: Boolean): Seq[DiscoveryInstance]

  /**
   * Get qualified instances within specified clusters of service
   *
   * @param serviceName name of service
   * @param clusters    list of cluster
   * @param healthy     a flag to indicate returning healthy or unhealthy instances
   * @return A qualified list of instance
   *
   */
  def selectInstances(serviceName: String, clusters: Seq[String], healthy: Boolean): Seq[DiscoveryInstance]

  /**
   * Get qualified instances within specified clusters of service
   *
   * @param serviceName name of service
   * @param clusters    list of cluster
   * @param healthy     a flag to indicate returning healthy or unhealthy instances
   * @param subscribe   if subscribe the service
   * @return A qualified list of instance
   *
   */
  def selectInstances(
      serviceName: String,
      clusters: Seq[String],
      healthy: Boolean,
      subscribe: Boolean): Seq[DiscoveryInstance]

  /**
   * Select one healthy instance of service using predefined load balance strategy
   *
   * @param serviceName name of service
   * @return qualified instance
   *
   */
  def selectOneHealthyInstance(serviceName: String): DiscoveryInstance

  /**
   * select one healthy instance of service using predefined load balance strategy
   *
   * @param serviceName name of service
   * @param subscribe   if subscribe the service
   * @return qualified instance
   *
   */
  def selectOneHealthyInstance(serviceName: String, subscribe: Boolean): DiscoveryInstance

  /**
   * Select one healthy instance of service using predefined load balance strategy
   *
   * @param serviceName name of service
   * @param clusters    a list of clusters should the instance belongs to
   * @return qualified instance
   *
   */
  def selectOneHealthyInstance(serviceName: String, clusters: Seq[String]): DiscoveryInstance

  /**
   * Select one healthy instance of service using predefined load balance strategy
   *
   * @param serviceName name of service
   * @param clusters    a list of clusters should the instance belongs to
   * @param subscribe   if subscribe the service
   * @return qualified instance
   *
   */
  def selectOneHealthyInstance(serviceName: String, clusters: Seq[String], subscribe: Boolean): DiscoveryInstance

  /**
   * Subscribe service to receive events of instances alteration
   *
   * @param serviceName name of service
   * @param listener    event listener
   *
   */
  def subscribe(serviceName: String, listener: DiscoveryEvent => Unit): Unit

  /**
   * subscribe service to receive events of instances alteration
   *
   * @param serviceName name of service
   * @param clusters    list of cluster
   * @param listener    event listener
   *
   */
  def subscribe(serviceName: String, clusters: Seq[String], listener: DiscoveryEvent => Unit): Unit

  /**
   * unsubscribe event listener of service
   *
   * @param serviceName name of service
   * @param listener    event listener
   *
   */
  def unsubscribe(serviceName: String, listener: DiscoveryEvent => Unit): Unit

  /**
   * unsubscribe event listener of service
   *
   * @param serviceName name of service
   * @param clusters    list of cluster
   * @param listener    event listener
   *
   */
  def unsubscribe(serviceName: String, clusters: Seq[String], listener: DiscoveryEvent => Unit): Unit

  /**
   * get all service names from server
   *
   * @param pageNo   page index
   * @param pageSize page size
   * @return list of service names
   */
  def getServicesOfServer(pageNo: Int, pageSize: Int): DiscoveryList[String]

  /**
   * Get all subscribed services of current client
   *
   * @return subscribed services
   */
  def getSubscribeServices: Seq[DiscoveryServiceInfo]

  /**
   * get server health status
   *
   * @return is server healthy
   */
  def getServerStatus: String
}
