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

import akka.Done
import akka.actor.typed.ActorSystem
import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.typed.ClusterSingleton
import akka.cluster.typed.SingletonActor
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.extension.FusionExtensionId
import fusion.scheduler.grpc.SchedulerService
import fusion.scheduler.model.End
import fusion.scheduler.service.SchedulerServiceImpl
import fusion.scheduler.service.actor.SchedulerBehavior

import scala.concurrent.Future

class SchedulerAggregate private (override val system: ActorSystem[_]) extends FusionExtension {

  // 使用Akka Cluster Singleton保证调度服务Actor在集群中只启动并活跃一个
  private val schedulerProxy = ClusterSingleton(system).init(
    SingletonActor(
      Behaviors.supervise(SchedulerBehavior()).onFailure[Exception](SupervisorStrategy.restart),
      SchedulerBehavior.name))

  FusionCore(system).shutdowns.serviceRequestsDone("fusion-scheduler") { () =>
    schedulerProxy ! End
    Future.successful(Done)
  }

  val schedulerService: SchedulerService = new SchedulerServiceImpl(schedulerProxy)(system.scheduler)
}

object SchedulerAggregate extends FusionExtensionId[SchedulerAggregate] {
  override def createExtension(system: ActorSystem[_]): SchedulerAggregate = new SchedulerAggregate(system)
}
