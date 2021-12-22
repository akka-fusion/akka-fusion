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

package fusion.cloud.discovery

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.discovery.Discovery
import com.orbitz.consul.option.{ ImmutableQueryOptions, QueryOptions }
import fusion.cloud.consul.discovery.FusionCloudDiscoveryConsul
import org.scalatest.OptionValues
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since v0.0.1 2021-08-13 16:36:35
 */
class DiscoveryHttpUtilsTest extends ScalaTestWithActorTestKit with AnyWordSpecLike with OptionValues {

  private val serviceDiscovery = Discovery(system).discovery

  "DiscoveryHttpUtils" should {
    "addresses" in {
      val serviceName = "xuanwu-visual"
      val resolved = serviceDiscovery.lookup(serviceName, 2.seconds).futureValue
      println("Office Discovery: " + resolved)

      val catalogClient = FusionCloudDiscoveryConsul(system).cloudConfig.fusionConsul.consul.catalogClient()
      val response = catalogClient.getService(serviceName, ImmutableQueryOptions.builder().build())
      response.getResponse.forEach(println)
    }
  }
}
