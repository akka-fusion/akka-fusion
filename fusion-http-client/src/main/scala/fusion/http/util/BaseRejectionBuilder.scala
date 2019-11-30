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

package fusion.http.util

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Directives.extractUri
import akka.http.scaladsl.server.RejectionHandler.Builder
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import fusion.http.rejection.ForbiddenRejection
import fusion.http.rejection.SessionRejection

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-01.
 */
object BaseRejectionBuilder extends StrictLogging {
  import fusion.http.util.HttpUtils._

  // #rejectionBuilder
  def rejectionBuilder: Builder =
    RejectionHandler
      .newBuilder()
      .handle {
        case MissingQueryParamRejection(parameterName) =>
          complete(jsonEntity(BadRequest, s"请求参数 '$parameterName' 缺失"))

        case MissingCookieRejection(cookieName) =>
          val msg = s"无效的Cookie: $cookieName"
          logger.info(msg)
          complete(jsonEntity(BadRequest, msg))

        case ForbiddenRejection(message, cause) =>
          val msg = s"权限禁止：$message"
          logger.warn(msg, cause.orNull)
          complete(jsonEntity(Forbidden, message))

        case SessionRejection(message, cause) =>
          val msg = s"会话认证失败：$message"
          logger.warn(msg, cause.orNull)
          complete(jsonEntity(Unauthorized, msg))

        case AuthorizationFailedRejection =>
          val msg = "会话认证失败"
          logger.warn(msg)
          complete(jsonEntity(Unauthorized, msg))

        case ValidationRejection(err, _) =>
          val msg = "数据校验失败： " + err
          logger.info(msg)
          complete(jsonEntity(BadRequest, msg))
      }
      .handleAll[MethodRejection] { methodRejections =>
        val description = methodRejections.map(_.supported.name).mkString(" or ")
        val msg = s"不支持的方法！当前支持：$description!"
        logger.info(msg)
        complete(jsonEntity(MethodNotAllowed, msg))
      }
      .handleNotFound {
        extractUri { uri =>
          val msg = s"URI: $uri 路径未找到！"
          logger.info(msg)
          complete(jsonEntity(NotFound, msg))
        }
      }
      .handle {
        case rejection =>
          logger.info(rejection.toString)
          complete(jsonEntity(BadRequest, rejection.toString))
      }
  // #rejectionBuilder

  final val rejectionHandler: RejectionHandler = rejectionBuilder.result()
}
