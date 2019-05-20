package fusion.discovery.http

import akka.http.scaladsl.model.Uri
import fusion.test.FusionTestFunSuite

class HttpClientTest extends FusionTestFunSuite {

  test("testUrl") {
    val uri = Uri("http://hongka-server-account/api/v4/account/credential/login")
    println(uri.authority.host.address())
  }

}
