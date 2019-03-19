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
