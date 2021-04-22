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

import akka.actor.typed.SpawnProtocol
import akka.http.scaladsl.server.Directives._
import fusion.cloud.FusionConfigFactory

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-01-11 21:39:52
 */
object GatewayApplication {

  def main(args: Array[String]): Unit = {
    val system = FusionConfigFactory.fromByConfig().initActorSystem(SpawnProtocol())
    implicit val classicSystem = system.classicSystem

    val route = complete("Hello world!")
    val binding = FusionHttpServer(system).component.startRouteSync(route)
    println(binding)
    //    FusionWeb(system).startAsync(route)
  }
}
