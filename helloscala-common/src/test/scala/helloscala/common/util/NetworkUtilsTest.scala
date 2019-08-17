package helloscala.common.util

import org.scalatest.FunSuite
import org.scalatest.MustMatchers

class NetworkUtilsTest extends FunSuite with MustMatchers {
  test("onlineNetworkInterfaces") {
    NetworkUtils.onlineNetworkInterfaces().foreach(println)
  }
  test("onlineInterfaceAddress") {
    val ias = NetworkUtils.onlineInterfaceAddress()
    ias must not be empty
    ias.foreach(println)
  }
}
