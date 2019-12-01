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

package fusion.job

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import fusion.common.extension.{ FusionExtension, FusionExtensionId }

class FusionJob private (override val system: ActorSystem[_]) extends FusionExtension {
  val components = new FusionJobComponents(system)
  CoordinatedShutdown(classicSystem).addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "FusionJob") { () =>
    components.closeAsync()(system.executionContext)
  }
  def component: FusionScheduler = components.component
}

object FusionJob extends FusionExtensionId[FusionJob] {
  override def createExtension(system: ActorSystem[_]): FusionJob = new FusionJob(system)
}
