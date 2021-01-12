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
import com.helloscala.akka.security.oauth.jacksons.ResponseTypeDeserializer
import com.helloscala.akka.security.oauth.jacksons.ResponseTypeSerializer

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-23 12:40:13
 */
@JsonSerialize(using = classOf[ResponseTypeSerializer])
@JsonDeserialize(using = classOf[ResponseTypeDeserializer])
sealed abstract class ResponseType(val VALUE: String) {
  override def toString: String = VALUE
}

object ResponseType {
  case object CODE extends ResponseType("code")
  case object TOKEN extends ResponseType("token")

  val values: Set[ResponseType] = Set(CODE, TOKEN)

  def valueOf(text: String): Option[ResponseType] = values.find(_.VALUE == text)
}
