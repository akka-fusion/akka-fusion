package fusion.discovery.client.nacos

import fusion.test.FusionTestFunSuite
import helloscala.common.Configuration

class ConfigurationTest extends FusionTestFunSuite {
  test("fusion.jdbc.default") {
    val configuration = Configuration.parseString("""fusion.jdbc {
                              |  default {
                              |    poolName = "hongka_openapi"
                              |    jdbcUrl = "jdbc:postgresql://10.0.0.14:5432/hongka_openapi"
                              |    username = "devuser"
                              |    password = "devpass.2019"
                              |    connectionTestQuery = "select 1;"
                              |    maximumPoolSize = 10
                              |  }
                              |}
                              |""".stripMargin)
    val c             = configuration.getConfig("fusion.jdbc.default")
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
