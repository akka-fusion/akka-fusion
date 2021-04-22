package com.helloscala.akka.security.oauth.server

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.MissingFormFieldRejection
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.helloscala.akka.security.core.UsernamePassword
import com.helloscala.akka.security.exception.AkkaSecurityException
import com.helloscala.akka.security.oauth.core.endpoint.OAuth2AuthorizationLogin
import com.helloscala.akka.security.oauth.server.authentication.OAuth2AccessTokenAuthenticationToken
import com.helloscala.akka.security.oauth.server.authentication.client.RegisteredClient
import com.helloscala.akka.security.oauth.server.crypto.keys.KeyManager
import com.helloscala.akka.security.oauth.server.crypto.keys.KeyUtils
import com.helloscala.akka.security.oauth.server.crypto.keys.ManagedKey
import com.helloscala.akka.security.oauth.server.directives.OAuth2Directive
import com.nimbusds.jose.jwk.JWKSet

import scala.collection.immutable
import scala.concurrent.duration._

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 16:31:19
 */
class OAuth2Route(system: ActorSystem[_]) extends OAuth2Directive {
  private def oauth2Extension = OAuth2Extension(system)
  def route: Route =
    pathPrefix("oauth2") {
      (path("token") & post) {
        oauth2TokenAuthentication
      } ~
      (path("jwks") & get) {
        jwks
      } ~
      path("authorize") {
        oauth2Authorize
      } ~
      path("login") {
        loginHtml ~
        login
      }
    }

  def login: Route = post {
    parameter("authorizeId") { authorizeId =>
      extractUsernameAndPassword { up =>
        // TODO Determine whether the username and password match

        // TODO get OAuth2AuthorizationLogin from actor with authorizeId
        val authorizationLogin: OAuth2AuthorizationLogin = null

        // TODO get RegisteredClient from actor with authorizationRequest#clientId
        val registeredClient: RegisteredClient = null

        val redirectUri =
          authorizationLogin.redirectUri.getOrElse(throw new AkkaSecurityException("The redirect_uri is required."))
        if (!registeredClient.redirectUris.contains(redirectUri)) {
          throw new AkkaSecurityException("Invalid redirect_uri.")
        }

        val uri = Uri(redirectUri).withQuery(
          Uri.Query("code" -> "code", "state" -> authorizationLogin.state.getOrElse(""), "scope" -> "scope"))

        if (up.username == "user1" && up.password == "password")
          redirect(uri, StatusCodes.Found)
        else
          redirect(uri, StatusCodes.Found)
      }
    }
  }

  private def extractUsernameAndPassword: Directive1[UsernamePassword] = entity(as[FormData]).flatMap { formData =>
    val usernameEither = formData.fields.get("username").toRight(MissingFormFieldRejection("username"))
    val passwordEither = formData.fields.get("password").toRight(MissingFormFieldRejection("password"))
    val list: immutable.Seq[Either[MissingFormFieldRejection, String]] = List(usernameEither, passwordEither)
    val errors = list.flatMap(_.left.toOption)
    if (errors.nonEmpty) {
      reject(errors: _*)
    } else {
      provide(UsernamePassword(usernameEither.right.get, passwordEither.right.get, ""))
    }
  }

  def loginHtml: Route = getFromResource("templates/login.html", ContentTypes.`text/html(UTF-8)`)

  def oauth2Authorize: Route = extractOAuth2Authorize { authentication =>
    oauth2Extension.oauth2AuthorizeProvider
    complete(authentication.toString)
  }

  def oauth2TokenAuthentication: Route =
    extractOAuth2TokenAuthentication { authentication =>
      val authenticationTokenFuture = oauth2Extension.clientCredentialsAuthenticationProvider
        .authenticate(authentication)
        .mapTo[OAuth2AccessTokenAuthenticationToken]
      onSuccess(authenticationTokenFuture) { authenticationToken =>
        complete(authenticationToken.token.toHttpEntity)
      }
    }

  def jwks: Route = {
    import scala.jdk.CollectionConverters._
    implicit val scheduler = system.scheduler
    implicit val timeout: Timeout = 5.seconds
    val keysFuture = oauth2Extension.keyManager.ask[Set[ManagedKey]](replyTo => KeyManager.FindAll(replyTo))
    onSuccess(keysFuture) { keys =>
      val jwks =
        keys.view.filter(managedKey => managedKey.isActive && managedKey.isAsymmetric).flatMap(KeyUtils.convert).toList
      val jwkSet = new JWKSet(jwks.asJava)
      complete(HttpEntity(ContentTypes.`application/json`, jwkSet.toString))
    }
  }
}
