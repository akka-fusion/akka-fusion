package fusion.discovery.client.nacos

import fusion.test.FusionTestFunSuite

class NacosServiceTest extends FusionTestFunSuite {

  test("testConfigService") {
    val configService = NacosService.configService("127.0.0.1:8848")
    val confStr = configService.getConfig("nacos.cfg.dataId", "test", 3000)
    confStr must not be empty
    println(confStr)
  }

  test("testNamingService") {}

}
