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

package fusion.cloud.consul

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{ BeforeAndAfterAll, OptionValues }

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-12-01 22:22:30
 */
class FusionConsulTest extends AnyWordSpec with Matchers with BeforeAndAfterAll with OptionValues with ScalaFutures {
  val fusionConsul = FusionConsul("localhost", 8500)

  "FusionConsul" should {
    "kv" in {
      val value = fusionConsul.getValueAsString("yj/local/auth-server.conf").value
      println(value)
    }

    "getConfig" in {
      val config = fusionConsul.getConfig("yj/local/auth-server.conf")
      config.getString("fusion.consul.test.description") shouldBe "consul"
    }
  }

  override protected def afterAll(): Unit = {
    fusionConsul.close()
    super.afterAll()
  }
}
