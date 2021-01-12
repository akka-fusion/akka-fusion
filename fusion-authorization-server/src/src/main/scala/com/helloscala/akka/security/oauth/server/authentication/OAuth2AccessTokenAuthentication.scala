package com.helloscala.akka.security.oauth.server.authentication

import akka.http.scaladsl.model.headers.HttpCredentials
import com.helloscala.akka.security.authentication.Authentication
import com.helloscala.akka.security.authentication.AuthenticationToken
import com.helloscala.akka.security.oauth.core.GrantType
import com.helloscala.akka.security.oauth.core.OAuth2AccessToken
import com.helloscala.akka.security.oauth.server.authentication.client.RegisteredClient

/**
 * OAuth 2 access token
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 16:32:24
 */
case class OAuth2AccessTokenAuthentication(
    grantType: GrantType,
    credentials: HttpCredentials,
    scopes: Set[String],
    parameters: Map[String, String])
    extends Authentication {
  override val isAuthenticated: Boolean = false
}

case class OAuth2ClientCredentialsAuthentication(
    registeredClient: RegisteredClient,
    grantType: GrantType,
    scopes: Set[String],
    authenticated: Boolean)
    extends Authentication {
  override def isAuthenticated: Boolean = authenticated
}

case class OAuth2AccessTokenAuthenticationToken(registeredClient: RegisteredClient, token: OAuth2AccessToken)
    extends AuthenticationToken
