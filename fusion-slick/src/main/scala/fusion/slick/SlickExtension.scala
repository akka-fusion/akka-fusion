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

package fusion.slick

import akka.actor.typed.{ ActorSystem, Extension, ExtensionId }
import fusion.jdbc.FusionJdbc
import slick.jdbc.JdbcBackend
import slick.util.AsyncExecutor

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date   2021-04-26 16:19:21
 */
class SlickExtension()(implicit system: ActorSystem[_]) extends Extension {
  val db = JdbcBackend.Database.forDataSource(FusionJdbc(system).component, Some(2), AsyncExecutor("msg", 2, 2, 100))
  system.classicSystem.registerOnTermination { db.close() }
}

object SlickExtension extends ExtensionId[SlickExtension] {
  override def createExtension(system: ActorSystem[_]): SlickExtension = new SlickExtension()(system)
}
