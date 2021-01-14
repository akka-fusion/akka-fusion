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

package com.helloscala.akka.security.oauth.core.endpoint

import com.helloscala.akka.security.oauth.core.GrantType
import com.helloscala.akka.security.oauth.core.ResponseType

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-23 12:39:41
 */
case class OAuth2AuthorizationLogin(
    authorizationUri: String,
    grantType: GrantType,
    responseType: ResponseType,
    clientId: String,
    redirectUri: Option[String],
    scopes: Set[String],
    state: Option[String]
)
