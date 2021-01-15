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

import helloscala.common.util.StringUtils

object SaltPassword {
  val SALT_LENGTH = 12
  val SALT_PASSWORD_LENGTH = 64
}

final case class SaltPassword(salt: String, saltPassword: String) {
  require(
    StringUtils.isNoneBlank(salt) && salt.length == SaltPassword.SALT_LENGTH,
    s"salt字符串长度必需为${SaltPassword.SALT_LENGTH}")
  require(
    StringUtils.isNoneBlank(saltPassword) && saltPassword.length == SaltPassword.SALT_PASSWORD_LENGTH,
    s"salt字符串长度必需为${SaltPassword.SALT_PASSWORD_LENGTH}")
}

final case class ByteSaltPassword(salt: Array[Byte], saltPassword: Array[Byte])
