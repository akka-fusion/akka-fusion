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

import akka.http.scaladsl.server.Rejection
import com.helloscala.akka.security.oauth.constant.OAuth2ParameterNames

import scala.collection.mutable

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-23 14:31:31
 */
case class OAuth2Error(errorCode: String, description: String, uri: String)

object OAuth2Error {

  def generateQuery(query: Map[String, String], errors: Seq[Rejection], state: Option[String]): Map[String, String] = {
    val buf = mutable.Map[String, String]()
    for ((key, value) <- query) {
      buf.put(key, value)
    }
    buf.put(OAuth2ParameterNames.ERROR, "400")
    if (errors.nonEmpty) {
      buf.put(OAuth2ParameterNames.ERROR_DESCRIPTION, errors.mkString("; "))
    }
    buf.put(OAuth2ParameterNames.ERROR_URI, "")
    state.foreach(value => buf.put(OAuth2ParameterNames.STATE, value))
    buf.toMap
  }
}
