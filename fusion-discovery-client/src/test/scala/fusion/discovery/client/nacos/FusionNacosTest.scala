package fusion.discovery.client.nacos

import java.util.Properties
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import com.alibaba.nacos.api.NacosFactory
import fusion.core.constant.PropKeys
import fusion.discovery.DiscoveryUtils
import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration
import org.scalatest.BeforeAndAfterAll

import scala.language.existentials

class FusionNacosTest extends FusionTestFunSuite with BeforeAndAfterAll {
  private var system: ActorSystem = _

  private val SERVER_ADDR  = "localhost:8848"
  private val NAMESPACE    = "5b764784-f457-46fb-96c6-4f086d5d0ce1"
  private val DATA_ID      = "hongka.file.app"
  private val GROUP        = NacosConstants.DEFAULT_GROUP
  private val SERVICE_NAME = "hongka-file-app"

  test("ConfigService") {
    val props = new Properties()
    props.setProperty("serverAddr", SERVER_ADDR)
    props.setProperty("namespace", NAMESPACE)
    val configService = NacosFactory.createConfigService(props)
//    val configService = DiscoveryUtils.defaultConfigService
    val confStr = configService.getConfig(DATA_ID, GROUP, 3000)
    confStr must not be null
  }

  test("configuration") {
    val configuration = Configuration().getConfiguration(DiscoveryUtils.methodConfPath)
    configuration.getString(PropKeys.SERVER_ADDR) mustBe SERVER_ADDR
    configuration.getString(PropKeys.NAMESPACE) mustBe NAMESPACE
    configuration.getString(PropKeys.DATA_ID) mustBe DATA_ID
  }

  test("ddd") {
    val clz = Option(Class.forName("fusion.discovery.DiscoveryUtils"))
      .getOrElse(Class.forName("fusion.discovery.DiscoveryUtils$"))
    val service          = clz.getMethod("defaultConfigService").invoke(null)
    val clzConfigService = Class.forName("fusion.discovery.client.FusionConfigService")
    val result = clzConfigService
      .getMethod("getConfig", classOf[String], classOf[String], classOf[Long])
      .invoke(service, DATA_ID, GROUP, Long.box(3000))
    println(result)
  }

  test("FusionNacos") {
    val confStr = FusionNacos(system).component.configService.getConfig
    println(confStr)

    confStr must not be null
    val configuration =
      Configuration.parseString(confStr).getConfiguration(DiscoveryUtils.methodConfPath)
    configuration.getString(PropKeys.SERVER_ADDR) mustBe SERVER_ADDR
    configuration.getString(PropKeys.NAMESPACE) mustBe NAMESPACE
    configuration.getString(PropKeys.DATA_ID) mustBe DATA_ID
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    System.setProperty("fusion.discovery.nacos." + PropKeys.SERVER_ADDR, SERVER_ADDR)
    System.setProperty("fusion.discovery.nacos." + PropKeys.NAMESPACE, NAMESPACE)
    System.setProperty("fusion.discovery.nacos." + PropKeys.SERVICE_NAME, SERVICE_NAME)
    System.setProperty("fusion.discovery.nacos." + PropKeys.TIMEOUT_MS, "3000")
    System.setProperty("fusion.name", DATA_ID)
    val configuration = Configuration.fromDiscovery()
    system = ActorSystem("test", configuration.underlying)
  }

  override protected def afterAll(): Unit = {
    TimeUnit.SECONDS.sleep(2)
    system.terminate()
    super.afterAll()
  }

}
