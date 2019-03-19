package fusion.discovery.client.nacos

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration
import org.scalatest.BeforeAndAfterAll

class FusionNacosTest extends FusionTestFunSuite with BeforeAndAfterAll {
  private var system: ActorSystem = _

  private val SERVER_ADDR = "123.206.9.104:8849"
  private val NAMESPACE = "3cc379e7-d0c0-461c-9700-abe252c60151"
  private val DATA_ID = "hongka.resource.app"
  private val GROUP = "DEFAULT_GROUP"
  private val SERVICE_NAME = "hongka.resource.app"

  /*
  test("ConfigService") {
    val props = new Properties()
    props.setProperty("serverAddr", SERVER_ADDR)
    props.setProperty("namespace", NAMESPACE)
//    val configService = NacosFactory.createConfigService(props)
    val configService = DiscoveryUtils.defaultConfigService
    val confStr = configService.getConfig(DATA_ID, GROUP, 3000)
    println(confStr)
    confStr must not be null
  }

  test("configuration") {
    val configuration = Configuration()
    configuration.getString(NacosConstants.CONF_SERVER_ADDR) mustBe SERVER_ADDR
    configuration.getString(NacosConstants.CONF_NAMESPACE) mustBe NAMESPACE
    configuration.getString(NacosConstants.CONF_DATA_ID) mustBe DATA_ID
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
   */

  test("FusionNacos") {
    val confStr = FusionNacos(system).configService.getConfig
    println(confStr)

    confStr must not be null
    val configuration = Configuration.parseString(confStr)
    configuration.getString(NacosConstants.CONF_SERVER_ADDR) mustBe SERVER_ADDR
    configuration.getString(NacosConstants.CONF_NAMESPACE) mustBe NAMESPACE
    configuration.getString(NacosConstants.CONF_DATA_ID) mustBe DATA_ID
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
//    System.setProperty(NacosConstants.CONF_SERVER_ADDR, SERVER_ADDR)
//    System.setProperty(NacosConstants.CONF_NAMESPACE, NAMESPACE)
//    System.setProperty(NacosConstants.CONF_SERVICE_NAME, SERVICE_NAME)
//    System.setProperty(NacosConstants.CONF_TIMEOUT_MS, "3000")
//    System.setProperty("fusion.name", DATA_ID)
    val configuration = Configuration.fromDiscovery()
    system = ActorSystem("test", configuration.underlying)
  }

  override protected def afterAll(): Unit = {
    TimeUnit.MINUTES.sleep(2)
    system.terminate()
    super.afterAll()
  }

}
