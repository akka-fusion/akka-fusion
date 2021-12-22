package com.helloscala.akka.security.oauth.server.directives

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.helloscala.akka.security.oauth.constant.OAuth2ParameterNames
import com.helloscala.akka.security.oauth.core.GrantType
import com.helloscala.akka.security.oauth.core.OAuth2Error
import com.helloscala.akka.security.oauth.core.ResponseType
import com.helloscala.akka.security.oauth.core.endpoint.OAuth2AuthorizationLogin
import com.helloscala.akka.security.oauth.server.authentication.OAuth2AccessTokenAuthentication

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-09-19 11:08:36
 */
trait OAuth2Directive {

  def extractOAuth2Authorize: Directive1[OAuth2AuthorizationLogin] =
    extractRequest.flatMap { request =>
      val query = request.uri.query()
      val redirectUri = query.get(OAuth2ParameterNames.REDIRECT_URI)
      val state = query.get(OAuth2ParameterNames.STATE)
      val clientIdEither = getClientId(query)
      val responseTypeEither = getResponseType(query)
      val errors: List[Rejection] = List(clientIdEither, responseTypeEither).flatMap(_.left.toOption)
      if (errors.nonEmpty) {
        sendErrorResponse(errors, state, redirectUri)
      } else {
        val grantType: GrantType =
          query.get(OAuth2ParameterNames.GRANT_TYPE).flatMap(GrantType.valueOf).getOrElse(GrantType.AUTHORIZATION_CODE)
        val scopes = query.get(OAuth2ParameterNames.SCOPE).map(_.split(' ').toSet).getOrElse(Set())
        val req = OAuth2AuthorizationLogin(
          request.uri.path.toString(),
          grantType,
          responseTypeEither.right.get,
          clientIdEither.right.get,
          redirectUri,
          scopes,
          state)
        provide(req)
      }
    }

  def sendErrorResponse(errors: Seq[Rejection], state: Option[String], redirectUri: Option[String]): StandardRoute = {
    redirectUri match {
      case Some(value) =>
        val uri = Uri(value)
        val query = Uri.Query(OAuth2Error.generateQuery(uri.query().toMap, errors, state))
        redirect(uri.withQuery(query), StatusCodes.Found)
      case None => reject(errors: _*)
    }
  }

  def extractOAuth2TokenAuthentication: Directive1[OAuth2AccessTokenAuthentication] =
    headerValueByType(Authorization).flatMap { authorization =>
      entity(as[FormData]).flatMap { formData =>
        val grantTypeEither = getGrantType(formData)
        val errors: Seq[Rejection] = List(grantTypeEither).flatMap(_.left.toOption)
        if (errors.nonEmpty) {
          reject(errors: _*)
        } else {
          val authentication = OAuth2AccessTokenAuthentication(
            grantTypeEither.right.get,
            authorization.credentials,
            formData.fields.get(OAuth2ParameterNames.SCOPE).map(_.split(' ').toSet).getOrElse(Set()),
            formData.fields.toMap)
          provide(authentication)
        }
      }
    }

  protected def getResponseType(query: Uri.Query): Either[Rejection, ResponseType] = {
    query
      .get(OAuth2ParameterNames.RESPONSE_TYPE)
      .toRight(MissingQueryParamRejection(OAuth2ParameterNames.RESPONSE_TYPE))
      .flatMap(text =>
        ResponseType
          .valueOf(text)
          .toRight(
            InvalidRequiredValueForQueryParamRejection(
              OAuth2ParameterNames.RESPONSE_TYPE,
              ResponseType.values.mkString(" or "),
              text)))
  }

  protected def getClientId(query: Uri.Query): Either[MissingQueryParamRejection, String] = {
    query
      .get(OAuth2ParameterNames.CLIENT_ID)
      .filter(_.nonEmpty)
      .toRight(MissingQueryParamRejection(OAuth2ParameterNames.CLIENT_ID))
  }

  protected def getGrantType(formData: FormData): Either[Rejection, GrantType] = {
    formData.fields
      .get(OAuth2ParameterNames.GRANT_TYPE)
      .toRight(MissingQueryParamRejection(OAuth2ParameterNames.GRANT_TYPE))
      .flatMap(text =>
        GrantType
          .valueOf(text)
          .toRight(
            InvalidRequiredValueForQueryParamRejection(
              OAuth2ParameterNames.GRANT_TYPE,
              GrantType.values.mkString(" or "),
              text)))
  }
}
