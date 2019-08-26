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

trait FusionConfigService {

  /**
   * Get config
   *
   * @param dataId    dataId
   * @param group     group
   * @param timeoutMs read timeout
   * @return config value
   */
  def getConfig(dataId: String, group: String, timeoutMs: Long): String
  def getConfig: String

  /**
   * Add a listener to the configuration, after the server modified the
   * configuration, the client will use the incoming listener callback.
   * Recommended asynchronous processing, the application can implement the
   * getExecutor method in the ManagerListener, provide a thread pool of
   * execution. If provided, use the main thread callback, May block other
   * configurations or be blocked by other configurations.
   *
   * @param dataId   dataId
   * @param group    group
   * @param listener listener
   */
  def addListener(dataId: String, group: String, listener: String => Unit): Unit

  /**
   * Publish config.
   *
   * @param dataId  dataId
   * @param group   group
   * @param content content
   * @return Whether publish
   */
  def publishConfig(dataId: String, group: String, content: String): Boolean

  /**
   * Remove config
   *
   * @param dataId dataId
   * @param group  group
   * @return whether remove
   */
  def removeConfig(dataId: String, group: String): Boolean

  /**
   * Remove listener
   *
   * @param dataId   dataId
   * @param group    group
   * @param listener listener
   */
  def removeListener(dataId: String, group: String, listener: String => Unit): Unit

  /**
   * Get server status
   *
   * @return whether health
   */
  def getServerStatus: String

}
