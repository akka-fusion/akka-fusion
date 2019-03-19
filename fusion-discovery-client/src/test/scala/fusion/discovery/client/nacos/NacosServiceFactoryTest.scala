package fusion.discovery.client.nacos

import java.util.Properties

import com.typesafe.config.ConfigFactory
import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration

class NacosServiceFactoryTest extends FusionTestFunSuite {

  // #NacosServiceFactoryTest
  test("通过Server地址直接访问") {
    val configService = NacosServiceFactory.configService("123.206.9.104:8849")
    val confStr = configService.getConfig("hongka.file.app", "DEFAULT_GROUP", 3000)
    confStr must not be empty
    val config = ConfigFactory.parseString(confStr).resolve()
    config.getString("fusion.name") mustBe "file-local"
  }

  test("通过namespace访问") {
    val props = new Properties()
    props.put("serverAddr", "123.206.9.104:8849")
    props.put("namespace", "7bf36554-e291-4789-b5fb-9e515ca58ba0")

    val configService = NacosServiceFactory.configService(props)
    val confStr = configService.getConfig("hongka.file.app", "DEFAULT_GROUP", 3000)
    confStr must not be empty
    ConfigFactory.invalidateCaches()
    val config = ConfigFactory.parseString(confStr).resolve()
    config.getString("fusion.name") mustBe "file-app"
  }

  test("尝试发现配置，失败读本地配置") {
    val props = sys.props
    props.put("fusion.discovery.enable", "true")
    props.put("fusion.discovery.nacos.serverAddr", "123.206.9.104:8849")
    props.put("fusion.discovery.nacos.namespace", "7bf36554-e291-4789-b5fb-9e515ca58ba0")
    props.put("fusion.discovery.nacos.dataId", "hongka.file.app")
//    props.put("fusion.discovery.nacos.group", "DEFAULT_GROUP")
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
