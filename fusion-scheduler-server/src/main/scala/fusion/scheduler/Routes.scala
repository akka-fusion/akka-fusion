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

package fusion.scheduler

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import fusion.core.extension.FusionCore
import fusion.scheduler.route.SchedulerRoute
import fusion.http.server.AbstractRoute
import fusion.scheduler.grpc.SchedulerServiceHandler

class Routes()(implicit system: ActorSystem[_]) extends AbstractRoute {
  implicit private val mat = Materializer(system)
  private val aggregate = SchedulerAggregate(system)
  private val grpcHandler = SchedulerServiceHandler(aggregate.schedulerService)(mat, FusionCore(system).classicSystem)

  override def route: Route = {
    pathPrefix("api" / "v4") {
      new SchedulerRoute(system).route
    } ~
    path("echo") {
      extractRequest { request =>
        system.log.info(s"echo receive message: $request")
        complete(request.toString())
      }
    } ~
    grpcRoute
  }

  def grpcRoute: Route = extractRequest { request =>
    complete(grpcHandler(request))
  }

}
