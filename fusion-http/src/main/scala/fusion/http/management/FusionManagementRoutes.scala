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

package fusion.http.management

import akka.actor.ExtendedActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.management.scaladsl.ManagementRouteProvider
import akka.management.scaladsl.ManagementRouteProviderSettings
import fusion.common.constant.ConfigKeys
import fusion.http.util.HttpUtils
import helloscala.common.Configuration
import helloscala.common.IntStatus

import scala.concurrent.Await
import scala.concurrent.duration._

class FusionManagementRoutes(system: ExtendedActorSystem) extends ManagementRouteProvider {
  override def routes(settings: ManagementRouteProviderSettings): Route = pathPrefix("fusion") {
    shutdownRoute ~
    healthRoute
  }

  def shutdownRoute: Route = (path("shutdown") & post) {
    val d = 1.second
    val msg = s"${d}后开始关闭Fusion系统"
    new Thread(new Runnable {
      override def run(): Unit = {
        Thread.sleep(d.toMillis)
        system.terminate()
        val atMost =
          Configuration(system.settings.config).get[Duration](s"${ConfigKeys.AKKA_MANAGEMENT_FUSION}.terminate-timeout")
        Await.ready(system.whenTerminated, atMost)
      }
    }).start()
    complete(HttpUtils.entityJson(s"""{"status":${IntStatus.OK},"msg":"$msg"}"""))
  }

  def healthRoute: Route = (path("health") & get) {
    complete(StatusCodes.OK)
  }

}
