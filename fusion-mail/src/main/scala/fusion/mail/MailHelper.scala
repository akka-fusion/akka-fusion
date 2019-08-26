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

import java.io.InputStream
import java.util.Properties

import javax.mail.internet.MimeMessage
import javax.mail._

class MailHelper(props: Properties, authenticator: Authenticator) extends AutoCloseable {
  private val session = Session.getDefaultInstance(props, authenticator)

  def user: String = props.getProperty("mail.smtp.user")
  def password: String = props.getProperty("mail.smtp.password")

  def createMimeMessage: MimeMessage = new MimeMessage(session)
  def createMimeMessage(is: InputStream): MimeMessage = new MimeMessage(session, is)

  /**
   * 发送邮件
   *
   * @param msg 邮件消息
   * @param user 发送人
   * @param password 发送人密码
   */
  def send(msg: Message, user: String, password: String): Unit = {
    Transport.send(msg, user, password)
  }

  def send(msg: Message): Unit = send(msg, user, password)

  override def close(): Unit = {}
}
