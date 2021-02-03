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

package com.helloscala.akka.security.oauth.jwt

import java.time.Instant

import com.helloscala.akka.security.oauth.core.TraitOAuth2Token

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 18:53:39
 */
case class Jwt(tokenValue: String, header: JwtHeader = JwtHeader(), claims: JwtClaims = JwtClaims())
    extends TraitOAuth2Token {
  override def issuedAt: Instant = claims.iss

  override def expiresAt: Instant = claims.exp
}

case class JwtHeader(
    alg: String = "",
    typ: String = "",
    cty: String = "",
    crit: Set[String] = Set(),
    customParams: Map[String, Object] = Map())

case class JwtClaims(
    iss: Instant = Instant.MIN,
    exp: Instant = Instant.MAX,
    sub: String = "",
    aud: Seq[String] = Seq(),
    nbf: Instant = Instant.MAX,
    jti: String = "",
    claims: Map[String, Object] = Map())
