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

import java.util.Properties

import com.typesafe.config.ConfigFactory
import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration

class NacosServiceFactoryTest extends FusionTestFunSuite {

  // #NacosServiceFactoryTest
  test("通过serverAddr地址和namespace直接访问") {
    val configService = NacosServiceFactory.configService("localhost:8848", "5b764784-f457-46fb-96c6-4f086d5d0ce1")
    val confStr = configService.getConfig("hongka.file.app", NacosConstants.DEFAULT_GROUP, 3000)
    confStr must not be empty
    val config = ConfigFactory.parseString(confStr).resolve()
    config.getString("fusion.name") mustBe "file-local"
  }

  test("通过Properties访问") {
    val props = new Properties()
    props.put("serverAddr", "localhost:8848")
    props.put("namespace", "5b764784-f457-46fb-96c6-4f086d5d0ce1")

    val configService = NacosServiceFactory.configService(props)
    val confStr = configService.getConfig("hongka.file.app", NacosConstants.DEFAULT_GROUP, 3000)
    confStr must not be empty
    ConfigFactory.invalidateCaches()
    val config = ConfigFactory.parseString(confStr).resolve()
    config.getString("fusion.name") mustBe "file-local"
  }

  test("尝试发现配置，失败读本地配置") {
    val props = sys.props
    props.put("fusion.discovery.enable", "true")
    props.put("fusion.discovery.nacos.serverAddr", "123.206.9.104:8849")
    props.put("fusion.discovery.nacos.namespace", "7bf36554-e291-4789-b5fb-9e515ca58ba0")
    props.put("fusion.discovery.nacos.dataId", "hongka.file.app")
//    props.put("fusion.discovery.nacos.group", NacosConstants.DEFAULT_GROUP)
    val configuration = Configuration.fromDiscovery()
    configuration.getString("fusion.name") mustBe "file-app"
  }
  // #NacosServiceFactoryTest

  test("config") {
    ConfigFactory.load().hasPath("aaa.bbb") mustBe false
    sys.props.put("aaa.bbb", "ok")
    ConfigFactory.invalidateCaches()
    ConfigFactory.load().getString("aaa.bbb") mustBe "ok"
  }
}
