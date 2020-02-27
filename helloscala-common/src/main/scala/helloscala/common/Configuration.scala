/*
 * Copyright 2019 akka-fusion.com
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

package helloscala.common

import java.util.{ Objects, Properties }

import com.typesafe.config._
import com.typesafe.config.impl.ConfigurationHelper
import com.typesafe.scalalogging.StrictLogging
import helloscala.common.exception.HSException

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.control.NonFatal

/**
 * Typesafe Config 辅助类
 *
 * @param underlying 原始Config, @see https://github.com/typesafehub/config.
 */
final case class Configuration(underlying: Config) {
  def discoveryEnable(): Boolean = getOrElse("fusion.discovery.enable", false)

  def withFallback(config: Config): Configuration = new Configuration(underlying.withFallback(config))

  def withFallback(config: Configuration): Configuration = new Configuration(underlying.withFallback(config.underlying))

  def computeIfMap[T, R](path: String, func: T => R)(implicit o: ConfigLoader[Option[T]]): Option[R] = {
    get[Option[T]](path).map(v => func(v))
  }

  def computeIfFlatMap[T, R](path: String, func: T => Option[R])(implicit o: ConfigLoader[Option[T]]): Option[R] = {
    get[Option[T]](path).flatMap(v => func(v))
  }

  def computeIfForeach[T](path: String, func: T => Unit)(implicit o: ConfigLoader[Option[T]]): Unit = {
    get[Option[T]](path).foreach(v => func(v))
  }

  /**
   * 合并两个HlConfiguration
   */
  def ++(other: Configuration): Configuration =
    new Configuration(other.underlying.withFallback(underlying))

  /**
   * Reads a value from the underlying implementation.
   * If the value is not set this will return None, otherwise returns Some.
   *
   * Does not check neither for incorrect type nor null value, but catches and wraps the error.
   */
  private def readValue[T](path: String, v: => T): Option[T] =
    try {
      if (underlying.hasPathOrNull(path)) Some(v) else None
    } catch {
      case NonFatal(e) =>
        throw new IllegalArgumentException(path + e.getMessage, e)
    }

  /**
   * Check if the given path exists.
   */
  def hasPath(path: String): Boolean = underlying.hasPath(path)

  def getConfiguration(path: String): Configuration = get[Configuration](path)

  def getConfig(path: String): Config = get[Config](path)

  def getArray(path: String): Array[String] = get[Array[String]](path)

  def getString(path: String): String = get[String](path)

  def getBytes(path: String): Long = underlying.getBytes(path)

  def getInt(s: String): Int = underlying.getInt(s)

  def getBoolean(path: String): Boolean = underlying.getBoolean(path)

  def getDuration(path: String): java.time.Duration =
    underlying.getDuration(path)

  def getOptionString(path: String): Option[String] = get[Option[String]](path)

  def getProperties(path: String): Properties = get[Properties](path)

  def getMap(path: String): Map[String, String] = get[Map[String, String]](path)

  def getJavaMap(path: String): java.util.Map[String, String] =
    get[java.util.Map[String, String]](path)

  /**
   * Get the config at the given path.
   */
  def get[A](path: String)(implicit loader: ConfigLoader[A]): A =
    loader.load(underlying, path)

  def getOrElse[A](path: String, deft: => A)(implicit loader: ConfigLoader[A]): A = get[Option[A]](path).getOrElse(deft)

  /**
   * Get the config at the given path and validate against a set of valid values.
   */
  def getAndValidate[A](path: String, values: Set[A])(implicit loader: ConfigLoader[A]): A = {
    val value = get(path)
    if (!values(value)) {
      throw reportError(path, s"Incorrect value, one of (${values.mkString(", ")}) was expected.")
    }
    value
  }

  /**
   * Get a value that may either not exist or be null. Note that this is not generally considered idiomatic Config
   * usage. Instead you should define all config keys in a reference.conf file.
   */
  def getOptional[A](path: String)(implicit loader: ConfigLoader[A]): Option[A] =
    readValue(path, get[A](path))

  /**
   * Retrieves a configuration value as `Milliseconds`.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val timeout = configuration.getMillis("engine.timeout")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.timeout = 1 second
   * }}}
   */
  def getMillis(path: String): Long = get[Duration](path).toMillis

  /**
   * Retrieves a configuration value as `Milliseconds`.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val timeout = configuration.getNanos("engine.timeout")
   * }}}
   *
   * The configuration must be provided as:
   *
   * {{{
   * engine.timeout = 1 second
   * }}}
   */
  def getNanos(path: String): Long = get[Duration](path).toNanos

  /**
   * Returns available keys.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val keys = configuration.keys
   * }}}
   *
   * @return the set of keys available in this configuration
   */
  def keys: Set[String] = underlying.entrySet.asScala.map(_.getKey).toSet

  /**
   * Returns sub-keys.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * val subKeys = configuration.subKeys
   * }}}
   *
   * @return the set of direct sub-keys available in this configuration
   */
  def subKeys: Set[String] = underlying.root().keySet().asScala.toSet

  /**
   * Returns every path as a set of key to value pairs, by recursively iterating through the
   * config objects.
   */
  def entrySet: Set[(String, ConfigValue)] = underlying.entrySet().asScala.map(e => e.getKey -> e.getValue).toSet

  /**
   * Creates a configuration error for a specific configuration key.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * throw configuration.reportError("engine.connectionUrl", "Cannot connect!")
   * }}}
   *
   * @param path    the configuration key, related to this error
   * @param message the error msg
   * @param e       the related exception
   * @return a configuration exception
   */
  def reportError(path: String, message: String, e: Option[Throwable] = None): HSException = {
    val origin = Option(
      if (underlying.hasPath(path)) underlying.getValue(path).origin
      else underlying.root.origin)
    Configuration.configError(message, origin, e)
  }

  /**
   * Creates a configuration error for this configuration.
   *
   * For example:
   * {{{
   * val configuration = HlConfiguration.load()
   * throw configuration.globalError("Missing configuration key: [yop.url]")
   * }}}
   *
   * @param message the error msg
   * @param e       the related exception
   * @return a configuration exception
   */
  def globalError(message: String, e: Option[Throwable] = None): HSException =
    Configuration.configError(message, Option(underlying.root.origin), e)

  override def toString: String = underlying.toString
}

object Configuration extends StrictLogging {
  private val KEY = "fusion.discovery.enable"
  private val SERVICE_NAME_KEY = "fusion.discovery.nacos.serviceName"

  // #fromDiscovery
  def fromDiscovery(): Configuration = {
    import scala.language.existentials
    ConfigFactory.invalidateCaches()
    val c = ConfigFactory.load()
    setServiceName(c)
    val enable = if (c.hasPath(KEY)) c.getBoolean(KEY) else false
    val config = if (enable) {
      try {
        val clz = Option(Class.forName("fusion.discovery.DiscoveryUtils"))
          .getOrElse(Class.forName("fusion.discovery.DiscoveryUtils$"))
        val service = clz.getMethod("defaultConfigService").invoke(null)
        val clzConfigService = Class.forName("fusion.discovery.client.FusionConfigService")
        val value = clzConfigService.getMethod("getConfig").invoke(service)
        val confStr = Objects.requireNonNull(value, "未能获取到配置内容").toString
        logger.info(s"收到配置内容：$confStr")
        parseString(confStr)
      } catch {
        case e: ReflectiveOperationException =>
          logger.info(s"服务发现组件缺失，使用本地默认配置", e)
          Configuration.load()
        case e: Throwable =>
          logger.warn("拉取配置内容失败，使用本地默认配置", e)
          Configuration.load()
      }
    } else {
      val configFrom = Option(System.getProperty("config.file"))
        .map(_ => "-Dconfig.file")
        .orElse(Option(System.getProperty("config.resource")).map(_ => "-Dconfig.resource"))
        .orElse(Option(System.getProperty("config.url")).map(_ => "-Dconfig.url"))
        .getOrElse("Jar包内部")
      logger.info(s"使用本地配置，来自：$configFrom")
      Configuration.load()
    }
    logger.trace(s"合并后配置内容：${config.underlying}")
    config
  }
  // #fromDiscovery

  private def setServiceName(c: Config): Unit = {
    if (c.hasPath(SERVICE_NAME_KEY)) {
      val serviceName = c.getString(SERVICE_NAME_KEY)
      logger.info(s"设置 serviceName: $serviceName")
      System.setProperty(SERVICE_NAME_KEY, serviceName)
    }
  }

  def load(config: Config): Configuration = {
    val c = ConfigFactory.defaultOverrides().withFallback(config)
    ConfigFactory.invalidateCaches()
    // TODO generateConfig() 需要？
    new Configuration(c.withFallback(generateConfig()).resolve())
  }

  def load(): Configuration = load(generateConfig())

  def load(props: Properties): Configuration = load(ConfigurationHelper.fromProperties(props))

  def parseString(content: String): Configuration = {
    load(ConfigFactory.parseString(content))
  }

  def configError(message: String, origin: Option[ConfigOrigin], me: Option[Throwable]): HSException = {
    val msg = origin.map(o => s"[$o] $message").getOrElse(message)
    me.map(e => new HSException(msg, e)).getOrElse(new HSException(msg))
  }

  def generateConfig(): Config = {
    if (noConfigProperties()) {
      val url = Thread.currentThread().getContextClassLoader.getResource("application-test.conf")
      if (Objects.isNull(url)) ConfigFactory.load()
      else {
        logger.info(
          "Resource file 'application-test.conf' found and didn't set config properties, use resource file 'application-test.conf'.")
        ConfigFactory.load("application-test.conf")
      }
    } else ConfigFactory.load()
  }

  def noConfigProperties(): Boolean = {
    var specified = 0
    if (System.getProperty("config.resource") != null) specified += 1
    if (System.getProperty("config.file") != null) specified += 1
    if (System.getProperty("config.url") != null) specified += 1
    specified == 0
  }
}
