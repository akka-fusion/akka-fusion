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
import akka.actor.typed.ActorSystem
import com.zaxxer.hikari.HikariDataSource
import fusion.core.component.Components
import fusion.core.extension.FusionCore
import fusion.core.extension.FusionExtension
import fusion.core.extension.FusionExtensionId
import fusion.jdbc.constant.JdbcConstants
import fusion.jdbc.util.JdbcUtils
import helloscala.common.Configuration

import scala.concurrent.Future

// #JdbcComponents
final private[jdbc] class JdbcComponents(system: ActorSystem[_])
    extends Components[HikariDataSource](JdbcConstants.PATH_DEFAULT) {
  override def configuration: Configuration = FusionCore(system).configuration

  override protected def componentClose(c: HikariDataSource): Future[Done] = Future.successful {
    c.close()
    Done
  }

  override protected def createComponent(id: String): HikariDataSource =
    JdbcUtils.createHikariDataSource(configuration.getConfig(id))
}
// #JdbcComponents

// #FusionJdbc
class FusionJdbc private (override val system: ActorSystem[_]) extends FusionExtension {
  val components = new JdbcComponents(system)
  FusionCore(system).shutdowns.beforeActorSystemTerminate("StopFusionJdbc") { () =>
    components.closeAsync()(system.executionContext)
  }
  def component: HikariDataSource = components.component
}

object FusionJdbc extends FusionExtensionId[FusionJdbc] {
  override def createExtension(system: ActorSystem[_]): FusionJdbc = new FusionJdbc(system)
}
// #FusionJdbc
