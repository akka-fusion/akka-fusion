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

package fusion.schedulerx

import com.typesafe.config.ConfigFactory
import helloscala.common.config.FusionConfigFactory
import org.scalatest.{ FunSuite, Matchers }

class SchedulerXSettingsTest extends FunSuite with Matchers {
  test("SchedulerXSettings") {
    val config =
      FusionConfigFactory.arrangeConfig(ConfigFactory.load("application-test.conf"), Constants.SCHEDULERX, Seq("akka"))
    val settings = SchedulerXSettings(config)
    settings.name should be(Constants.SCHEDULERX)
    config.getStringList("akka.cluster.roles") should contain(NodeRoles.WORKER)
    settings.isBroker should be(false)
    settings.isWorker should be(true)
  }
}
