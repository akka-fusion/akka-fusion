/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.security.util

import java.util

import helloscala.common.util.{DigestUtils, StringUtils, Utils}

object SecurityUtils {
  final val CLIENT_KEY_LENGTH = 32
  final val ENCODING_AES_KEY_LENGTH = 43

  //  def generateBizMsgCrypt(configuration: Configuration): HSBizMsgCrypt = {
  //    val key = configuration.getString("helloscala.crypt.client-key")
  //    val encodingAesKey = configuration.getString("helloscala.crypt.encoding-aes-key")
  //    val appId = configuration.getString("helloscala.crypt.client-id")
  //    new HSBizMsgCrypt(key, encodingAesKey, appId)
  //  }
  //
  //  def generateBizMsgCrypt(key: String, encodingAesKey: String, clientId: String): HSBizMsgCrypt =
  //    new HSBizMsgCrypt(key, encodingAesKey, clientId)

  /**
   * 生成通用 Salt 及 Salt Password
   *
   * @param password 待生成密码
   * @return
   */
  def byteGeneratePassword(password: String): ByteSaltPassword = {
    val salt = Utils.randomBytes(SaltPassword.SALT_LENGTH)
    val saltPwd = DigestUtils.sha256(salt ++ password.getBytes)
    ByteSaltPassword(salt, saltPwd)
  }

  def generatePassword(password: String): SaltPassword = {
    val salt = StringUtils.randomString(SaltPassword.SALT_LENGTH)
    val saltPwd = DigestUtils.sha256Hex(salt ++ password)
    SaltPassword(salt, saltPwd)
  }

  /**
   * 校验密码
   *
   * @param salt     salt
   * @param saltPwd  salt password
   * @param password request password
   * @return
   */
  def matchSaltPassword(salt: Array[Byte], saltPwd: Array[Byte], password: Array[Byte]): Boolean = {
    val bytes = DigestUtils.sha256(salt ++ password)
    util.Arrays.equals(saltPwd, bytes)
  }

  def matchSaltPassword(salt: String, saltPwd: String, password: String): Boolean = {
    val securityPassword = DigestUtils.sha256Hex(salt + password)
    securityPassword == saltPwd
  }
}
