package com.helloscala.akka.security.oauth.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.util.Timeout
import com.helloscala.akka.security.authentication.AuthenticationProvider
import com.helloscala.akka.security.oauth.server.authentication.OAuth2Authorize
import com.helloscala.akka.security.oauth.server.authentication.client.RegisteredClientRepository
import com.helloscala.akka.security.oauth.server.crypto.keys.KeyManager
import com.helloscala.akka.security.oauth.server.jwt.JwtEncoder
import com.helloscala.akka.security.util.AkkaUtils

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @since 2020-09-20 22:20:15
 */
class OAuth2AuthorizationServerConfigure(system: ActorSystem[_]) {
  implicit private val typedSystem = system
  implicit val timeout: Timeout = 5.seconds

  private var _registeredClientRepository: ActorRef[RegisteredClientRepository.Command] = _
  private var _jwtEncoder: ActorRef[JwtEncoder.Command] = _
  private var _keyManager: ActorRef[KeyManager.Command] = _
  private var _authorizationService: ActorRef[OAuth2AuthorizationService.Command] = _
  private var _clientCredentialsAuthenticationProvider: AuthenticationProvider = _

  def registeredClientRepository: ActorRef[RegisteredClientRepository.Command] = _registeredClientRepository

  def jwtEncoder: ActorRef[JwtEncoder.Command] = _jwtEncoder

  def keyManager: ActorRef[KeyManager.Command] = _keyManager

  def authorizationService: ActorRef[OAuth2AuthorizationService.Command] = _authorizationService

  def oauth2AuthorizeProvider: ActorRef[OAuth2Authorize.Command] = ???

  def clientCredentialsAuthenticationProvider: AuthenticationProvider = _clientCredentialsAuthenticationProvider

  def init(): Future[Unit] = {
    _registeredClientRepository = getRegisteredClientRepository()
    _jwtEncoder = getJwtEncoder()
    _keyManager = getKeyManager()
    _authorizationService = getAuthorizationService()
    _clientCredentialsAuthenticationProvider = getClientCredentialsAuthenticationProvider()
    Future.successful(())
  }

  def getRegisteredClientRepository(): ActorRef[RegisteredClientRepository.Command] =
    AkkaUtils.receptionistFindOneSync(RegisteredClientRepository.Key)

  def getJwtEncoder(): ActorRef[JwtEncoder.Command] = AkkaUtils.receptionistFindOneSync(JwtEncoder.Key)

  def getKeyManager(): ActorRef[KeyManager.Command] = AkkaUtils.receptionistFindOneSync(KeyManager.Key)

  def getAuthorizationService(): ActorRef[OAuth2AuthorizationService.Command] =
    AkkaUtils.receptionistFindOneSync(OAuth2AuthorizationService.Key)

  def getClientCredentialsAuthenticationProvider(): AuthenticationProvider =
    createInstanceFor[AuthenticationProvider]("akka.security.server.authentication-provider.client-credentials")

  protected def createInstanceFor[T: ClassTag](path: String): T = {
    val fqcn = system.settings.config.getString(path)
    system.dynamicAccess
      .createInstanceFor[T](fqcn, List(classOf[ActorSystem[_]] -> system))
      .orElse(system.dynamicAccess.createInstanceFor[T](fqcn, Nil))
      .getOrElse(throw new ExceptionInInitializerError(s"Initial $fqcn class error."))
  }

}
