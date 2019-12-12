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

package fusion.discoveryx.server.route

import akka.http.scaladsl.model.headers.`Timeout-Access`
import akka.http.scaladsl.model.{ HttpEntity, HttpRequest, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.typesafe.scalalogging.StrictLogging
import fusion.discoveryx.DiscoveryX
import fusion.discoveryx.common.Constants
import fusion.discoveryx.server.config.ConfigSettings
import fusion.discoveryx.server.naming.NamingSettings

class Routes(discoveryX: DiscoveryX, configSettings: ConfigSettings, namingSettings: NamingSettings)
    extends StrictLogging {
  def route: Route =
    pathPrefix("fusion" / Constants.DISCOVERYX / "v1") {
      new NamingRoute(discoveryX, namingSettings).route ~
      new ConfigRoute(discoveryX, configSettings).route
    } ~ new GrpcRoute(discoveryX, configSettings, namingSettings).route

  def logRequest = mapRequest { req =>
    curlLogging(req)
  }

  def curlLogging(req: HttpRequest): HttpRequest = {
    logger.whenDebugEnabled {
      val entity = req.entity match {
        case HttpEntity.Empty => ""
        case _                => "\n" + req.entity
      }
      val headers = req.headers.filterNot(_.name() == `Timeout-Access`.name)
      logger.info(s"""HttpRequest
                   |${req.protocol.value} ${req.method.value} ${req.uri}
                   |search: ${toString(req.uri.query())}
                   |header: ${headers.mkString("\n        ")}$entity""".stripMargin)
    }
    req
  }

  def toString(query: Uri.Query): String = query.map { case (name, value) => s"$name=$value" }.mkString("&")
}
