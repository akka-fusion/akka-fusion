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

package fusion.discovery.client.nacos

import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration

class ConfigurationTest extends FusionTestFunSuite {
  test("fusion.jdbc.default") {
    val configuration = Configuration.parseString("""akka.http {
  host-connection-pool {
    max-open-requests = 64
    max-retries = 0
  }
  server {
    idle-timeout = 120.seconds
    request-timeout = 90.seconds
    socket-options {
      tcp-keep-alive = on
    }
  }
  client {
    connecting-timeout = 60.seconds
    socket-options {
      tcp-keep-alive = on
    }
  }
}
""")
    val c = configuration.getConfig("akka.http.client")
    println("c is " + c)
//    val props = c.getProperties(null)
//    println(props)

  }

  test("configuration") {
    val configuration = Configuration.fromDiscovery()
    println(configuration.toString)
    println(configuration.getString("fusion.name"))
  }

}
