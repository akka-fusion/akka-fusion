/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.mail

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.TestKit
import fusion.test.FusionTestFunSuite
import javax.mail.Message.RecipientType
import org.scalatest.BeforeAndAfterAll

class FusionMailTest extends TestKit(ActorSystem("mail-test")) with FusionTestFunSuite with BeforeAndAfterAll {

  test("init") {
    val mailHelper = FusionMail(system).components.lookup("fusion.mail.wangyi")

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
