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

package com.helloscala.akka.security.oauth.core

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.helloscala.akka.security.oauth.jacksons.TokenTypeDeserializer
import com.helloscala.akka.security.oauth.jacksons.TokenTypeSerializer

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-09-19 09:24:53
 */
@JsonSerialize(using = classOf[TokenTypeSerializer])
@JsonDeserialize(using = classOf[TokenTypeDeserializer])
sealed abstract class TokenType(val VALUE: String) {
  override def toString: String = VALUE
}

object TokenType {
  case object BEARER extends TokenType("Bearer")

  val values: Set[TokenType] = Set(BEARER)

  def valueOf(text: String): Option[TokenType] = values.find(_.VALUE == text)
}
