package com.helloscala.akka.security.oauth.server.crypto.keys

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.receptionist.ServiceKey
import akka.actor.typed.scaladsl.ActorContext
import akka.actor.typed.scaladsl.Behaviors

/**
 * @author Yang Jing <a href="mailto:yang.xunjing@qq.com">yangbajing</a>
 * @date 2020-09-20 13:52:11
 */
object KeyManager {
  trait Command
  val Key: ServiceKey[Command] = ServiceKey[Command]("KeyManager")

  case class FindById(id: String, replyTo: ActorRef[Option[ManagedKey]]) extends Command
  case class FindByAlgorithm(algorithm: String, replyTo: ActorRef[Set[ManagedKey]]) extends Command
  case class FindAll(replyTo: ActorRef[Set[ManagedKey]]) extends Command
}

import com.helloscala.akka.security.oauth.server.crypto.keys.KeyManager._
class InMemoryKeyManager(context: ActorContext[Command]) {
  def receive(keys: Map[String, ManagedKey]): Behavior[Command] = Behaviors.receiveMessagePartial {
    case FindById(id, replyTo) =>
      replyTo ! keys.get(id)
      receive(keys)

    case FindByAlgorithm(algorithm, replyTo) =>
      replyTo ! keys.valuesIterator.filter(_.getAlgorithm == algorithm).toSet
      receive(keys)

    case FindAll(replyTo) =>
      replyTo ! keys.valuesIterator.toSet
      receive(keys)
  }
}
object InMemoryKeyManager {
  def apply(): Behavior[Command] = Behaviors.setup { context =>
    new InMemoryKeyManager(context).receive(KeyUtils.generateKeys())
  }
}
