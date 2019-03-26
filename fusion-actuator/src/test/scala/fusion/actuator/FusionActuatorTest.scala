package fusion.actuator

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import fusion.test.FusionTestFunSuite
import helloscala.common.jackson.Jackson

class FusionActuatorTest extends FusionTestFunSuite with ScalatestRouteTest {
  test("route") {
    val fusion      = FusionActuator(system)
    val route       = fusion.route
    val contextPath = fusion.actuatorSetting.contextPath
    Get(s"/$contextPath/health") ~> route ~> check {
      status mustBe StatusCodes.OK
      val text = responseAs[String]
      val json = Jackson.readTree(text)
      text must not be empty
      json.hasNonNull("details") mustBe true
    }
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    FusionActuator(system)
  }

}
