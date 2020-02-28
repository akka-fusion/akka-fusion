/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.discovery.client

import java.util.Objects

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Authority
import fusion.discovery.model.DiscoveryEvent
import fusion.discovery.model.DiscoveryInstance
import fusion.discovery.model.DiscoveryList
import fusion.discovery.model.DiscoveryServiceInfo

import scala.util.control.NonFatal

trait FusionNamingService {
  /**
   * register a instance to service
   *
   * @param serviceName name of service
   * @param ip          instance ip
   * @param port        instance port
   */
  def registerInstance(serviceName: String, ip: String, port: Int): DiscoveryInstance

  /**
   * register a instance to service with specified cluster name
   *
   * @param serviceName name of service
   * @param ip          instance ip
   * @param port        instance port
   * @param clusterName instance cluster name
   *
   */
  def registerInstance(serviceName: String, ip: String, port: Int, clusterName: String): DiscoveryInstance

  /**
   * register a instance to service with specified instance properties
   *
   * @param serviceName name of service
   * @param instance    instance to register
   *
   */
  def registerInstance(serviceName: String, instance: DiscoveryInstance): DiscoveryInstance

  def registerInstance(instance: DiscoveryInstance): DiscoveryInstance

  def registerInstanceCurrent(): DiscoveryInstance

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

  def deregisterInstance(instance: DiscoveryInstance): Unit

  def deregisterInstanceCurrent(): Unit

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

  def selectOneHealthUri(uri: Uri): Uri =
    uri.copy(authority = selectOneHealthToAuthority(uri.authority))

  def selectOneHealthUri(uri: Uri, serviceName: String): Uri =
    uri.copy(authority = selectOneHealthToAuthority(serviceName))

  def selectOneHealthUri(uri: Uri, authority: Authority): Uri =
    uri.copy(authority = selectOneHealthToAuthority(authority))

  def selectOneHealthHttpRequest(request: HttpRequest): HttpRequest =
    request.copy(uri = selectOneHealthUri(request.uri, request.uri.authority))

  def selectOneHealthHttpRequest(request: HttpRequest, serviceName: String): HttpRequest =
    request.copy(uri = selectOneHealthUri(request.uri, serviceName))

  def selectOneHealthToAuthority(authority: Authority): Authority = {
    if (authority.host.isNamedHost()) {
      try {
        val inst = selectOneHealthyInstance(authority.host.address())
        if (Objects.nonNull(inst)) Authority(Uri.Host(inst.ip), inst.port)
        else authority
      } catch {
        case NonFatal(_) => authority
      }
    } else {
      authority
    }
  }

  def selectOneHealthToAuthority(serviceName: String): Authority = {
    try {
      val inst = selectOneHealthyInstance(serviceName)
      if (Objects.nonNull(inst)) Authority(Uri.Host(inst.ip), inst.port)
      else Authority.parse(serviceName)
    } catch {
      case NonFatal(_) => Authority.parse(serviceName)
    }
  }
}
