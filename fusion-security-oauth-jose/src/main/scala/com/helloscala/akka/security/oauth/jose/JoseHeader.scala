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

package com.helloscala.akka.security.oauth.jose

import com.nimbusds.jose.Header
import com.nimbusds.jose.JWSHeader

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-20 13:46:52
 */
case class JoseHeader(header: Header) {

  def toJwsHeader(keyId: String): JWSHeader = {
    new JWSHeader.Builder(header.asInstanceOf[JWSHeader]).keyID(keyId).build()
  }
}
