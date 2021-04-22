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

package com.helloscala.akka.security.oauth.jacksons

import java.time.Duration

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.`type`.ReferenceType
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.fasterxml.jackson.databind.ser.Serializers
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationConfig
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.module.scala.JacksonModule
import com.helloscala.akka.security.oauth.core.OAuth2AccessToken

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 09:39:55
 */
trait OAuth2AccessTokenModule extends JacksonModule {
  this += OAuth2AccessTokenSerializerResolver
}

private object OAuth2AccessTokenSerializerResolver extends Serializers.Base {
  private val OAUTH2_ACCESS_TOKEN = classOf[OAuth2AccessToken]

  override def findSerializer(
      config: SerializationConfig,
      `type`: JavaType,
      beanDesc: BeanDescription): JsonSerializer[_] = {
    val rawClass = `type`.getRawClass
    if (OAUTH2_ACCESS_TOKEN.isAssignableFrom(rawClass))
      new OAuth2AccessTokenSerializer(OAUTH2_ACCESS_TOKEN)
    else
      null
  }

}

class OAuth2AccessTokenSerializer(vc: Class[OAuth2AccessToken]) extends StdSerializer[OAuth2AccessToken](vc) {

  override def serialize(value: OAuth2AccessToken, gen: JsonGenerator, provider: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("access_token", value.tokenValue)
    gen.writeStringField("scope", value.scopes.mkString(" "))
    gen.writeStringField("token_type", value.tokenType.VALUE)
    gen.writeNumberField("expires_in", Duration.between(value.issuedAt, value.expiresAt).toSeconds)
    gen.writeEndObject()
  }

}
