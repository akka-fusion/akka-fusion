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

package com.helloscala.akka.security.oauth.jacksons

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.SerializerProvider
import com.helloscala.akka.security.oauth.core.GrantType
import com.helloscala.akka.security.oauth.core.ResponseType
import com.helloscala.akka.security.oauth.core.TokenType

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 09:57:46
 */
trait OAuth2JacksonModule extends OAuth2AccessTokenModule

object OAuth2JacksonModule extends OAuth2JacksonModule

class TokenTypeSerializer extends StdSerializer[TokenType](classOf[TokenType]) {

  override def serialize(value: TokenType, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeString(value.VALUE)
}

class TokenTypeDeserializer extends StdDeserializer[TokenType](classOf[TokenType]) {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): TokenType =
    p.getText match {
      case TokenType.BEARER.VALUE => TokenType.BEARER
      case _                      => throw new JsonParseException(p, s"Invalid token type, currently only supported ${TokenType.BEARER}")
    }
}

class GrantTypeSerializer(vc: Class[GrantType]) extends StdSerializer[GrantType](vc) {

  override def serialize(value: GrantType, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeString(value.VALUE)
}

class GrantTypeDeserializer(vc: Class[GrantType]) extends StdDeserializer[GrantType](vc) {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): GrantType =
    GrantType.valueOf(p.getText).getOrElse(throw new JsonParseException(p, "Invalid authorization grant type."))
}

class ResponseTypeSerializer(vc: Class[ResponseType]) extends StdSerializer[ResponseType](vc) {

  override def serialize(value: ResponseType, gen: JsonGenerator, provider: SerializerProvider): Unit =
    gen.writeString(value.VALUE)
}

class ResponseTypeDeserializer(vc: Class[ResponseType]) extends StdDeserializer[ResponseType](vc) {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): ResponseType =
    ResponseType.valueOf(p.getText).getOrElse(throw new JsonParseException(p, "Invalid authorization response type."))
}
