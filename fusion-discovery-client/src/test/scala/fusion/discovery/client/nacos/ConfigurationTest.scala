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
    val c             = configuration.getConfig("akka.http.client")
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
