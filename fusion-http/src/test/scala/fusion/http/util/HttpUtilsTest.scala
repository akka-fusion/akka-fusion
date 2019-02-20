package fusion.http.util

import fusion.test.FusionTestFunSuite

class HttpUtilsTest extends FusionTestFunSuite {

  test("testForExtension") {
    HttpUtils.customMediaTypes must not be empty
    HttpUtils.customMediaTypes.values.toList.distinct.foreach { mt =>
      println(s"$mt; ${mt.binary}; ${mt.comp}; ${mt.fileExtensions.mkString(",")}")
    }
  }

}
