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

package docs

import akka.actor.ActorSystem
import fusion.actuator.FusionActuator
import fusion.discovery.client.nacos.FusionNacos
import fusion.http.FusionHttpServer

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.io.StdIn

object FusionNacosDemo extends App {
  implicit val system = ActorSystem("fusion-nacos-demo")
  val actuatorRoute = FusionActuator(system).route
  FusionHttpServer(system).component.startRouteSync(actuatorRoute)
  FusionNacos(system).component.namingService.registerInstance("hongka.file.converter", "192.168.1.53", 8000)

  StdIn.readLine()
  system.terminate()
  Await.result(system.whenTerminated, 10.seconds)
}
