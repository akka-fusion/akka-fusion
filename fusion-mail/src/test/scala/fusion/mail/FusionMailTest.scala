package fusion.mail

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite
import javax.mail.Message.RecipientType
import org.scalatest.BeforeAndAfterAll

class FusionMailTest extends TestKit(ActorSystem("mail-test")) with FusionTestFunSuite with BeforeAndAfterAll {

  test("init") {
    val mailHelper = FusionMail(system).component

    val msg = mailHelper.createMimeMessage
    msg.setFrom("devops@ihongka.cn")
    msg.addRecipients(RecipientType.TO, "yang.xunjing@qq.com")
    msg.setSubject("测试邮件哈哈哈")
    msg.setText("测试邮件内容——羊八井")

    mailHelper.send(msg)
  }

  override protected def afterAll(): Unit = {
    TimeUnit.SECONDS.sleep(10)
    super.afterAll()
  }
}
