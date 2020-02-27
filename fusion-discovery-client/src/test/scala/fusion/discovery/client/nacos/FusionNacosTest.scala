/*
 * Copyright 2019 akka-fusion.com
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
import java.util.concurrent.TimeUnit

import akka.actor.typed.scaladsl.adapter._
import akka.{ actor => classic }
import com.alibaba.nacos.api.NacosFactory
import fusion.common.constant.PropKeys
import fusion.discovery.DiscoveryUtils
import fusion.testkit.FusionFunSuiteLike
import helloscala.common.Configuration
import org.scalatest.BeforeAndAfterAll

import scala.language.existentials

class FusionNacosTest extends FusionFunSuiteLike with BeforeAndAfterAll {
  private var system: classic.ActorSystem = _

  private val SERVER_ADDR = "localhost:8848"
  private val NAMESPACE = "5b764784-f457-46fb-96c6-4f086d5d0ce1"
  private val DATA_ID = "fusion.file.app"
  private val GROUP = NacosConstants.DEFAULT_GROUP
  private val SERVICE_NAME = "fusion-file-app"

  test("ConfigService") {
    val props = new Properties()
    props.setProperty("serverAddr", SERVER_ADDR)
    props.setProperty("namespace", NAMESPACE)
    val configService = NacosFactory.createConfigService(props)
//    val configService = DiscoveryUtils.defaultConfigService
    val confStr = configService.getConfig(DATA_ID, GROUP, 3000)
    confStr should not be null
  }

  test("configuration") {
    val configuration = Configuration.load().getConfiguration(DiscoveryUtils.methodConfPath)
    configuration.getString(PropKeys.SERVER_ADDR) shouldBe SERVER_ADDR
    configuration.getString(PropKeys.NAMESPACE) shouldBe NAMESPACE
    configuration.getString(PropKeys.DATA_ID) shouldBe DATA_ID
  }

  test("ddd") {
    val clz = Option(Class.forName("fusion.discovery.DiscoveryUtils"))
      .getOrElse(Class.forName("fusion.discovery.DiscoveryUtils$"))
    val service = clz.getMethod("defaultConfigService").invoke(null)
    val clzConfigService = Class.forName("fusion.discovery.client.FusionConfigService")
    val result = clzConfigService
      .getMethod("getConfig", classOf[String], classOf[String], classOf[Long])
      .invoke(service, DATA_ID, GROUP, Long.box(3000))
    println(result)
  }

  test("FusionNacos") {
    val confStr = FusionNacos(system.toTyped).component.configService.getConfig
    println(confStr)

    confStr should not be null
    val configuration =
      Configuration.parseString(confStr).getConfiguration(DiscoveryUtils.methodConfPath)
    configuration.getString(PropKeys.SERVER_ADDR) shouldBe SERVER_ADDR
    configuration.getString(PropKeys.NAMESPACE) shouldBe NAMESPACE
    configuration.getString(PropKeys.DATA_ID) shouldBe DATA_ID
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("fusion.discovery.nacos." + PropKeys.SERVER_ADDR, SERVER_ADDR)
    System.setProperty("fusion.discovery.nacos." + PropKeys.NAMESPACE, NAMESPACE)
    System.setProperty("fusion.discovery.nacos." + PropKeys.SERVICE_NAME, SERVICE_NAME)
    System.setProperty("fusion.discovery.nacos." + PropKeys.TIMEOUT_MS, "3000")
    System.setProperty("fusion.name", DATA_ID)
    val configuration = Configuration.fromDiscovery()
    system = classic.ActorSystem("test", configuration.underlying)
  }

  override protected def afterAll(): Unit = {
    TimeUnit.SECONDS.sleep(2)
    system.terminate()
    super.afterAll()
  }
}
