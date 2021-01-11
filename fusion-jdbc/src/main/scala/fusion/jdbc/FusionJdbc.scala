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

package fusion.jdbc

import akka.Done
import akka.actor.ExtendedActorSystem
import com.zaxxer.hikari.HikariDataSource
import fusion.common.component.Components
import fusion.common.extension.{FusionExtension, FusionExtensionId}
import fusion.core.extension.FusionCore
import fusion.jdbc.constant.JdbcConstants
import fusion.jdbc.util.JdbcUtils
import helloscala.common.Configuration

import scala.concurrent.Future

// #JdbcComponents
final private[jdbc] class JdbcComponents(system: ExtendedActorSystem)
    extends Components[HikariDataSource](JdbcConstants.PATH_DEFAULT) {
  override def configuration: Configuration = Configuration(system.settings.config)

  override protected def componentClose(c: HikariDataSource): Future[Done] =
    Future.successful {
      c.close()
      Done
    }

  override protected def createComponent(id: String): HikariDataSource =
    JdbcUtils.createHikariDataSource(configuration.getConfig(id))
}
// #JdbcComponents

// #FusionJdbc
class FusionJdbc private (override val classicSystem: ExtendedActorSystem) extends FusionExtension {
  val components = new JdbcComponents(classicSystem)
  FusionCore(classicSystem).shutdowns.beforeActorSystemTerminate("StopFusionJdbc") { () =>
    components.closeAsync()(classicSystem.dispatcher)
  }
  def component: HikariDataSource = components.component
}

object FusionJdbc extends FusionExtensionId[FusionJdbc] {
  override def createExtension(system: ExtendedActorSystem): FusionJdbc = new FusionJdbc(system)
}
// #FusionJdbc
