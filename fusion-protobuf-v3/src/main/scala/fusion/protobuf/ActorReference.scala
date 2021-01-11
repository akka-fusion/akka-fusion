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

package fusion.protobuf

import akka.actor.ExtendedActorSystem
import akka.actor.typed.{ActorRef, ActorRefResolver}
import akka.serialization.Serialization
import akka.{actor => classic}
import fusion.core.FusionApplication
import helloscala.common.util.StringUtils
import scalapb.TypeMapper

import scala.reflect.ClassTag

trait ActorReferenceTrait {
  def serialized: String
  def typ: Option[String]

  def toTyped[T](implicit ev1: ClassTag[T]): ActorRef[T] = {
    typ match {
      case None                                       => throw new IllegalStateException("typ is empty")
      case Some(tp) if ev1.runtimeClass.getName != tp => throw new IllegalArgumentException(s"$typ != $tp")
      case _ =>
        ActorRefResolver(FusionApplication.application.typedSystem).resolveActorRef(serialized)
    }
  }

  def toClassic: classic.ActorRef =
    FusionApplication.application.classicSystem.asInstanceOf[ExtendedActorSystem].provider.resolveActorRef(serialized)
}

//trait ActorReferenceCompanion {
//  def fromTyped[T](ref: ActorRef[T])(implicit ev1: ClassTag[T]): fusion.ActorReference = {
//    fusion.ActorReference(
//      ActorRefResolver(ActorSystemUtils.system).toSerializationFormat(ref),
//      Some(ev1.runtimeClass.toString))
//  }
//  def fromClassic(ref: classic.ActorRef): String = Serialization.serializedActorPath(ref)
//}

trait ActorTypedReferenceTrait {
  def serialized: String

  def toTyped[T]: ActorRef[T] = ActorRefResolver(FusionApplication.application.typedSystem).resolveActorRef(serialized)

  def toClassic: classic.ActorRef =
    FusionApplication.application.classicSystem.asInstanceOf[ExtendedActorSystem].provider.resolveActorRef(serialized)
}

trait ActorTypedReferenceCompanion {

  def fromTyped[T](ref: ActorRef[T]): String =
    ActorRefResolver(FusionApplication.application.typedSystem).toSerializationFormat(ref)
  def fromClassic(ref: classic.ActorRef): String = Serialization.serializedActorPath(ref)
}

trait ActorRefCompanion {
  private def resolver: ActorRefResolver = ActorRefResolver(FusionApplication.application.typedSystem)

  implicit def actorRefTypeMapper[T]: TypeMapper[String, ActorRef[T]] = {
    TypeMapper[String, ActorRef[T]] { str =>
      if (StringUtils.isBlank(str)) FusionApplication.application.typedSystem.deadLetters[T]
      else resolver.resolveActorRef[T](str)
    } { ref =>
      //      resolver.toSerializationFormat(ref)
      ref.path.elements match {
        case List("deadLetters") => "" // resolver.toSerializationFormat(ActorSystemUtils.system.deadLetters[T])
        case _                   => resolver.toSerializationFormat(ref)
      }
    }
  }

//  implicit def actorRefTypeOptionfMapper[T]: TypeMapper[StringValue, ActorRef[T]] = {
//    val resolver: ActorRefResolver = ActorRefResolver(ActorSystemUtils.system)
//    TypeMapper[StringValue, ActorRef[T]] { maybe =>
//      resolver.resolveActorRef[T](maybe.value)
//    } { maybe =>
//      StringValue(resolver.toSerializationFormat(maybe))
//    }
//  }

//  implicit def actorRefTypeOptionMapper[T]: TypeMapper[StringValue, Option[ActorRef[T]]] = {
//    val resolver: ActorRefResolver = ActorRefResolver(ActorSystemUtils.system)
//    TypeMapper[StringValue, Option[ActorRef[T]]] { maybe =>
//      if (StringUtils.isBlank(maybe.value)) None
//      else Some(resolver.resolveActorRef[T](maybe.value))
//    } { maybe =>
//      maybe.map(ref => StringValue(resolver.toSerializationFormat(ref))).getOrElse(StringValue.defaultInstance)
//    }
//  }
}

object ActorRefCompanion extends ActorRefCompanion
