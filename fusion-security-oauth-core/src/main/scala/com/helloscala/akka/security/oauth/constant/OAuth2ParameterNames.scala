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

package com.helloscala.akka.security.oauth.constant

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 11:29:11
 */
object OAuth2ParameterNames {

  /**
   * {@code grant_type} - used in Access Token Request.
   */
  val GRANT_TYPE = "grant_type"

  /**
   * {@code response_type} - used in Authorization Request.
   */
  val RESPONSE_TYPE = "response_type"

  /**
   * {@code client_id} - used in Authorization Request and Access Token Request.
   */
  val CLIENT_ID = "client_id"

  /**
   * {@code client_secret} - used in Access Token Request.
   */
  val CLIENT_SECRET = "client_secret"

  /**
   * {@code redirect_uri} - used in Authorization Request and Access Token Request.
   */
  val REDIRECT_URI = "redirect_uri"

  /**
   * {@code scope} - used in Authorization Request, Authorization Response, Access Token Request and Access Token Response.
   */
  val SCOPE = "scope"

  /**
   * {@code state} - used in Authorization Request and Authorization Response.
   */
  val STATE = "state"

  /**
   * {@code code} - used in Authorization Response and Access Token Request.
   */
  val CODE = "code"

  /**
   * {@code access_token} - used in Authorization Response and Access Token Response.
   */
  val ACCESS_TOKEN = "access_token"

  /**
   * {@code token_type} - used in Authorization Response and Access Token Response.
   */
  val TOKEN_TYPE = "token_type"

  /**
   * {@code expires_in} - used in Authorization Response and Access Token Response.
   */
  val EXPIRES_IN = "expires_in"

  /**
   * {@code refresh_token} - used in Access Token Request and Access Token Response.
   */
  val REFRESH_TOKEN = "refresh_token"

  /**
   * {@code username} - used in Access Token Request.
   */
  val USERNAME = "username"

  /**
   * {@code password} - used in Access Token Request.
   */
  val PASSWORD = "password"

  /**
   * {@code error} - used in Authorization Response and Access Token Response.
   */
  val ERROR = "error"

  /**
   * {@code error_description} - used in Authorization Response and Access Token Response.
   */
  val ERROR_DESCRIPTION = "error_description"

  /**
   * {@code error_uri} - used in Authorization Response and Access Token Response.
   */
  val ERROR_URI = "error_uri"

  /**
   * Non-standard parameter (used internally).
   */
  val REGISTRATION_ID = "registration_id"
}
