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

package com.helloscala.akka.security.oauth.util

import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer

import scala.concurrent.Future

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 11:16:03
 */
object HttpUtils {

  def getParameterValues(request: HttpRequest, name: String)(implicit ec: Materializer): Future[List[String]] = {
    import ec.executionContext
    val query = request.uri.query()
    query.getAll(name) match {
      case Nil if request.entity.contentType == ContentTypes.`application/x-www-form-urlencoded` =>
        Unmarshal(request.entity).to[FormData].map(form => form.fields.getAll(name))
      case values => Future.successful(values)
    }
  }

}
