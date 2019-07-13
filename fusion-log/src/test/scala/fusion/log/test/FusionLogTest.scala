package fusion.log.test

import com.typesafe.scalalogging.StrictLogging
import fusion.test.FusionTestFunSuite

class FusionLogTest extends FusionTestFunSuite with StrictLogging {
  test("json logging") {
    logger.info("Fusion Log Test")
  }
}
