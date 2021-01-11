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

package fusion.log.logback

import java.net.InetAddress

import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import helloscala.common.util.{StringUtils, Utils}

object Converters {

  val hosts = List(
    () => System.getProperty("fusion.http.default.server.host"),
    () => System.getProperty("http.host"), // spring
    () => System.getProperty("server.host")
  )

  val ports = List(
    () => System.getProperty("fusion.http.default.server.port"),
    () => System.getProperty("http.port"), // spring
    () => System.getProperty("server.port")
  )

  val serviceNames = List(
    () => System.getProperty("fusion.name"),
    () => System.getProperty("spring.application.name"), // spring
    () => System.getProperty("fusion.service.name")
  )

  val envs = List(
    () => System.getProperty("fusion.profiles.active"),
    () => System.getProperty("spring.profiles.active"), // spring
    () => System.getProperty("run.env")
  )

  @inline final def valueFromFunctions(list: List[() => String], value: String): String =
    Utils.getValueFromFunctions[String](list, value, StringUtils.isNoneEmpty)
}

class LogHostNameConverter extends ClassicConverter {

  override def convert(event: ILoggingEvent): String =
    try {
      InetAddress.getLocalHost.getCanonicalHostName
    } catch {
      case _: Throwable =>
        ""
    }
}

import fusion.log.logback.Converters._

class LogHostConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = valueFromFunctions(hosts, null)
}

class LogPortConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = valueFromFunctions(ports, null)
}

class LogServiceNameConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = valueFromFunctions(serviceNames, null)
}

class LogEnvConverter extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = valueFromFunctions(envs, null)
}
