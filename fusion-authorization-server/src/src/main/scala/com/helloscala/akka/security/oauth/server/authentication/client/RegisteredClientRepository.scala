package com.helloscala.akka.security.oauth.server.authentication.client

import java.util.UUID

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.mutable

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-19 17:53:41
 */
object RegisteredClientRepository {
  trait Command
  val Key = ServiceKey[Command]("RegisteredClientRepository")

  case class FindById(id: String, replyTo: ActorRef[Option[RegisteredClient]]) extends Command
  case class FindByClientId(clientId: String, replyTo: ActorRef[Option[RegisteredClient]]) extends Command

}

import com.helloscala.akka.security.oauth.server.authentication.client.RegisteredClientRepository._
class InMemoryRegisteredClientRepository(context: ActorContext[Command]) {
  private val clients = List(
    RegisteredClient(
      UUID.randomUUID().toString,
      "messaging-client",
      "secret",
      Set(),
      Set("message.read", "message.write"),
      "rsa-key"),
    RegisteredClient(
      UUID.randomUUID().toString,
      "ec-client",
      "secret",
      Set(),
      Set("message.read", "message.write"),
      "ec-key"))
  private val clientIdRegisteredClientMap = mutable.Map[String, RegisteredClient]() ++ clients.map(v => v.clientId -> v)
  private val idRegisteredClientMap = mutable.Map[String, RegisteredClient]() ++ clients.map(v => v.id -> v)

  def receive(): Behavior[Command] =
    Behaviors.receiveMessagePartial {
      case FindById(id, replyTo) =>
        replyTo ! idRegisteredClientMap.get(id)
        Behaviors.same

      case FindByClientId(clientId, replyTo) =>
        replyTo ! clientIdRegisteredClientMap.get(clientId)
        Behaviors.same
    }
}
object InMemoryRegisteredClientRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new InMemoryRegisteredClientRepository(context).receive())
}
