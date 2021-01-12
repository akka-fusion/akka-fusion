package com.helloscala.akka.security.oauth.server

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors
import com.helloscala.akka.security.oauth.core.TokenType
import com.helloscala.akka.security.oauth.server.authentication.OAuth2AccessTokenAuthenticationToken

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 18:34:53
 */
object OAuth2AuthorizationService {
  trait Command
  trait Event extends Command
  val Key = ServiceKey[Command]("OAuth2AuthorizationService")

  case class FindByToken(
      token: String,
      tokenType: TokenType,
      replyTo: ActorRef[Option[OAuth2AccessTokenAuthenticationToken]])
      extends Command
  case class Save(authenticationToken: OAuth2AccessTokenAuthenticationToken) extends Event
}

import com.helloscala.akka.security.oauth.server.OAuth2AuthorizationService._
class InMemoryAuthorizationService(context: ActorContext[Command]) {
  def receive(tokens: Map[String, OAuth2AccessTokenAuthenticationToken]): Behavior[Command] =
    Behaviors.receiveMessagePartial {
      case Save(authenticationToken) =>
        receive(tokens + (authenticationToken.registeredClient.clientId -> authenticationToken))
      case FindByToken(token, tokenType, replyTo) =>
        replyTo ! tokens.valuesIterator.find(t => t.token.tokenValue == token && t.token.tokenType == tokenType)
        receive(tokens)
    }
}

object InMemoryAuthorizationService {
  def apply(): Behavior[Command] = Behaviors.setup(context => new InMemoryAuthorizationService(context).receive(Map()))
}
