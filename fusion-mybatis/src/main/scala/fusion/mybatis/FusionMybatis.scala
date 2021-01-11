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

package fusion.mybatis

import akka.actor.ExtendedActorSystem
import fusion.common.extension.{FusionExtension, FusionExtensionId}
import fusion.core.extension.FusionCore

class FusionMybatis private (override val classicSystem: ExtendedActorSystem) extends FusionExtension {
  val components: MybatisComponents = new MybatisComponents(classicSystem)
  def component: FusionSqlSessionFactory = components.component
  FusionCore(classicSystem).shutdowns.beforeActorSystemTerminate("StopFusionMybatis") { () =>
    components.closeAsync()(classicSystem.dispatcher)
  }
}

object FusionMybatis extends FusionExtensionId[FusionMybatis] {
  override def createExtension(system: ExtendedActorSystem): FusionMybatis = new FusionMybatis(system)
}
