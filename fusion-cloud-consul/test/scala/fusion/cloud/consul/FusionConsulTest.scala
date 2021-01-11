package fusion.cloud.consul

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{ BeforeAndAfterAll, OptionValues }

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-12-01 22:22:30
 */
class FusionConsulTest extends AnyWordSpec with Matchers with BeforeAndAfterAll with OptionValues with ScalaFutures {
  val fusionConsul = FusionConsul("localhost", 8500)

  "FusionConsul" should {
    "kv" in {
      val value = fusionConsul.getValueAsString("yj/local/auth-server.conf").value
      println(value)
    }

    "getConfig" in {
      val config = fusionConsul.getConfig("yj/local/auth-server.conf")
      config.getString("fusion.consul.test.description") shouldBe "consul"
    }
  }

  override protected def afterAll(): Unit = {
    fusionConsul.close()
    super.afterAll()
  }
}
