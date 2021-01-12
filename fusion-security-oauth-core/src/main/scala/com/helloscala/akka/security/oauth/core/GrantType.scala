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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.helloscala.akka.security.oauth.jacksons.GrantTypeDeserializer
import com.helloscala.akka.security.oauth.jacksons.GrantTypeSerializer

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 09:13:30
 */
@JsonSerialize(using = classOf[GrantTypeSerializer])
@JsonDeserialize(using = classOf[GrantTypeDeserializer])
sealed abstract class GrantType(val VALUE: String) {
  override def toString: String = VALUE
}

object GrantType {
  case object AUTHORIZATION_CODE extends GrantType("authorization_code")
  case object CLIENT_CREDENTIALS extends GrantType("client_credentials")
  case object REFRESH_TOKEN extends GrantType("refresh_token")
  case object PASSWORD extends GrantType("password")

  val values: Set[GrantType] = Set(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN, PASSWORD)

  def valueOf(text: String): Option[GrantType] = values.find(_.VALUE == text)
}
