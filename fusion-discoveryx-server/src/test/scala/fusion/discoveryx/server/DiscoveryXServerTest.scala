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

package fusion.discoveryx.server

import com.typesafe.config.ConfigFactory
import fusion.discoveryx.common.Constants
import helloscala.common.config.FusionConfigFactory
import org.scalatest.{ Matchers, WordSpecLike }

class DiscoveryXServerTest extends WordSpecLike with Matchers {
  private val config =
    FusionConfigFactory.arrangeConfig(ConfigFactory.load("application-test.conf"), Constants.DISCOVERYX)
  private val xserver = DiscoveryXServer(config)

  "DiscoveryXServerTest" should {
    "namingSetting" in {
      xserver.namingSetting
    }

    "configSetting" in {
      xserver.configSetting
    }

    "grpcHandler" in {
      xserver.grpcHandler should not be null
    }

    "start" in {
      xserver.start()
    }
  }
}
