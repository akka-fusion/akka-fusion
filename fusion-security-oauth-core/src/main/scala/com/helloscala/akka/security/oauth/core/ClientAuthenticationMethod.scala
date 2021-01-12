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

package com.helloscala.akka.security.oauth.core

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-23 17:16:21
 */
sealed abstract class ClientAuthenticationMethod(val VALUE: String) {
  override def toString: String = VALUE
}

object ClientAuthenticationMethod {
  case object BASIC extends ClientAuthenticationMethod("basic")
  case object POST extends ClientAuthenticationMethod("post")
  case object NONE extends ClientAuthenticationMethod("none")

  val values: Set[ClientAuthenticationMethod] = Set(BASIC, POST, NONE)

  def valueOf(text: String): Option[ClientAuthenticationMethod] = values.find(_.VALUE == text)
}
