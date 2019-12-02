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

package fusion.schedulerx.worker

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.typesafe.config.ConfigFactory
import fusion.schedulerx.{ NodeRoles, SchedulerXConfigFactory, SchedulerXSettings }
import org.scalatest.WordSpecLike

class SchedulerXWorkerImplTest extends ScalaTestWithActorTestKit with WordSpecLike {
  "SchedulerXWorker" must {
    "settings" in {
      val config = SchedulerXConfigFactory.arrangeConfig(ConfigFactory.load())
      val settings = SchedulerXSettings(config)
      settings.name should be("schedulerx")
      config.getStringList("akka.cluster.roles") should contain(NodeRoles.WORKER)
      settings.isBroker should be(false)
      settings.isWorker should be(true)
    }
  }
}
