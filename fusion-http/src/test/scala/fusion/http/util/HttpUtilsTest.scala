package fusion.http.util

import fusion.test.FusionTestFunSuite

class HttpUtilsTest extends FusionTestFunSuite {

  test("testForExtension") {
    HttpUtils.customMediaTypes must not be empty
    HttpUtils.customMediaTypes.map(_._2.binary) must contain(true)
  }

}
