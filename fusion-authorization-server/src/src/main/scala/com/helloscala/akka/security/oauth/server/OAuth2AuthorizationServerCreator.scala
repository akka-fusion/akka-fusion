package com.helloscala.akka.security.oauth.server

import akka.actor.typed.ActorRef
import akka.actor.typed._
import akka.actor.typed.receptionist.Receptionist
import akka.util.Timeout
import com.helloscala.akka.security.oauth.server.authentication.client.InMemoryRegisteredClientRepository
import com.helloscala.akka.security.oauth.server.authentication.client.RegisteredClientRepository
import com.helloscala.akka.security.oauth.server.crypto.keys.InMemoryKeyManager
import com.helloscala.akka.security.oauth.server.crypto.keys.KeyManager
import com.helloscala.akka.security.oauth.server.jwt.DefaultJwtEncoder
import com.helloscala.akka.security.oauth.server.jwt.JwtEncoder
import com.helloscala.akka.security.util.AkkaUtils

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._

class OAuth2AuthorizationServerCreator {
  protected implicit val timeout: Timeout = 5.seconds

  def createRegisteredClientRepository(implicit
      reactor: RecipientRef[SpawnProtocol.Command],
      scheduler: Scheduler): Future[ActorRef[RegisteredClientRepository.Command]] =
    AkkaUtils.spawn(InMemoryRegisteredClientRepository(), RegisteredClientRepository.Key.id)

  def createJwtEncoder(implicit
      reactor: RecipientRef[SpawnProtocol.Command],
      scheduler: Scheduler): Future[ActorRef[JwtEncoder.Command]] =
    AkkaUtils.spawn(DefaultJwtEncoder(), JwtEncoder.Key.id)

  def createKeyManager(implicit
      reactor: RecipientRef[SpawnProtocol.Command],
      scheduler: Scheduler): Future[ActorRef[KeyManager.Command]] =
    AkkaUtils.spawn(InMemoryKeyManager(), KeyManager.Key.id)

  def createOAuth2AuthorizationService(implicit
      reactor: RecipientRef[SpawnProtocol.Command],
      scheduler: Scheduler): Future[ActorRef[OAuth2AuthorizationService.Command]] =
    AkkaUtils.spawn(InMemoryAuthorizationService(), OAuth2AuthorizationService.Key.id)

  def init()(implicit
      reactor: RecipientRef[SpawnProtocol.Command],
      receptionist: ActorRef[Receptionist.Command],
      scheduler: Scheduler,
      ec: ExecutionContext): Future[_] = {
    val registeredClientRepositoryF = createRegisteredClientRepository.map(ref =>
      receptionist ! Receptionist.Register(RegisteredClientRepository.Key, ref))

    val futures = Seq(
      createKeyManager.map(ref => receptionist ! Receptionist.Register(KeyManager.Key, ref)),
      createJwtEncoder.map(ref => receptionist ! Receptionist.Register(JwtEncoder.Key, ref)),
      createOAuth2AuthorizationService.map(ref =>
        receptionist ! Receptionist.Register(OAuth2AuthorizationService.Key, ref)))

    registeredClientRepositoryF.flatMap(_ => Future.sequence(futures))
  }
}
object OAuth2AuthorizationServerCreator {
  def init(system: ActorSystem[SpawnProtocol.Command]): Future[_] = {
    new OAuth2AuthorizationServerCreator()
      .init()(system, system.receptionist, system.scheduler, system.executionContext)
  }
}
