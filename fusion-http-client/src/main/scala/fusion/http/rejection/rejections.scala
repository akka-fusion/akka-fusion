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

package fusion.http.rejection

import akka.http.scaladsl.server.RejectionWithOptionalCause

/**
 * 权限拒绝，403
 *
 * @param message 错误消息
 * @param cause 可选的异常
 */
case class ForbiddenRejection(message: String, cause: Option[Throwable] = None)
    extends akka.http.javadsl.server.AuthorizationFailedRejection
    with RejectionWithOptionalCause

/**
 * 会话认证拒绝，401
 * @param message 错误消息
 * @param cause 可选的异常
 */
case class SessionRejection(message: String, cause: Option[Throwable] = None)
    extends akka.http.javadsl.server.AuthorizationFailedRejection
    with RejectionWithOptionalCause
