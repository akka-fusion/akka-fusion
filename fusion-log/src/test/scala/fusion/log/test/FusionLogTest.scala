package fusion.log.test

import com.typesafe.scalalogging.StrictLogging
import fusion.test.FusionTestFunSuite
import helloscala.common.exception.HSBadRequestException

class FusionLogTest extends FusionTestFunSuite with StrictLogging {
  test("json logging") {
    logger.info("Fusion Log Test")
    logger.error("bad request", HSBadRequestException("bad request"))
  }
}
