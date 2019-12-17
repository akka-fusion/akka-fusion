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

package fusion.mybatis

import akka.actor.typed.ActorSystem
import fusion.common.extension.{ FusionCoordinatedShutdown, FusionExtension, FusionExtensionId }

class FusionMybatis private (override val system: ActorSystem[_]) extends FusionExtension {
  val components: MybatisComponents = new MybatisComponents(system)
  def component: FusionSqlSessionFactory = components.component
  FusionCoordinatedShutdown(system).beforeActorSystemTerminate("StopFusionMybatis") { () =>
    components.closeAsync()(system.executionContext)
  }
}

object FusionMybatis extends FusionExtensionId[FusionMybatis] {
  override def createExtension(system: ActorSystem[_]): FusionMybatis = new FusionMybatis(system)
}
