package fusion.http.util

import akka.http.scaladsl.model.Uri.Authority
import fusion.test.FusionTestFunSuite

class HttpUtilsTest extends FusionTestFunSuite {
  test("authority") {
    val a = Authority.parse("hongka-server-account")
    println(a)
    println(a.host)
    println(a.host.address())
  }

  test("testForExtension") {
    HttpUtils.customMediaTypes must not be empty
    HttpUtils.customMediaTypes.map(_._2.binary) must contain(true)
  }

  test("copyUri") {
//    val request = HttpUtils.copyUri(request, scheme, serviceName)
  }

}
