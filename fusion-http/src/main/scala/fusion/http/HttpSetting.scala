/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.http

import com.typesafe.config.Config
import com.typesafe.sslconfig.ssl.{ SSLConfigFactory, SSLConfigSettings }
import fusion.http.constant.HttpConstants
import helloscala.common.Configuration

final class HttpSetting private (c: Configuration, config: Config) {
  def exceptionHandler: String = c.getString("exception-handler")
  def rejectionHandler: String = c.getString("rejection-handler")
  def defaultInterceptor: Seq[String] = c.get[Seq[String]]("default-interceptor")
  def httpInterceptors: Seq[String] = c.get[Seq[String]]("http-interceptors")

  def createSSLConfig(): SSLConfigSettings = {
    val akkaOverrides = config.getConfig("akka.ssl-config")
    val defaults = config.getConfig("ssl-config")
    val mergeConfig = akkaOverrides.withFallback(defaults)
    val sslConfig = if (c.hasPath("ssl.ssl-config")) {
      c.getConfig("ssl.ssl-config").withFallback(mergeConfig)
    } else {
      mergeConfig
    }
    SSLConfigFactory.parse(sslConfig)
  }

  object server {

    def host: String =
      c.getOrElse("server.host", config.getString(HttpConstants.SERVER_HOST_PATH))

    def port: Int = {
      c.get[Option[Int]]("server.port") match {
        case Some(port) => port
        case _ =>
          if (!config.hasPath(HttpConstants.SERVER_PORT_PATH)) 0
          else config.getInt(HttpConstants.SERVER_PORT_PATH)
      }
    }
  }

  override def toString = s"HttpSetting(${c.underlying.root()})"
}

object HttpSetting {
  def apply(c: Configuration, config: Config): HttpSetting = new HttpSetting(c, config)
}
