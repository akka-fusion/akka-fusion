/*
 * Copyright 2019-2021 helloscala.com
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

package fusion.json.jackson

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import com.fasterxml.jackson.core.{JsonParseException, JsonParser}
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import helloscala.common.util.{EnumTrait, EnumTraitCompanion}
import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers

abstract class EnumTraitDeserHelper[A <: EnumTrait](cls: Class[A], comp: EnumTraitCompanion)
    extends StdDeserializer[A](cls) {

  override def deserialize(p: JsonParser, ctxt: DeserializationContext): A = {
    val value = p.getValueAsInt
    comp.fromValue(value) match {
      case Some(v) => v.asInstanceOf[A]
      case _       => throw new JsonParseException(p, s"Deserializer value $value to classOf[OrgType] is error.")
    }
  }
}

@JsonDeserialize(using = classOf[OrgTypes.EnumDeser])
sealed abstract class OrgType(override val companion: EnumTraitCompanion, override protected val value: Int)
    extends EnumTrait

object OrgTypes extends EnumTraitCompanion {
  self =>
  override type Value = OrgType

  final case object SCHOOL extends OrgType(self, 1)

  final case object COMPANY extends OrgType(self, 2)

  registerEnums(SCHOOL, COMPANY)
  class EnumDeser extends EnumTraitDeserHelper(classOf[OrgType], self)
}

class EnumTraitTest extends ScalaTestWithActorTestKit with AnyFunSuiteLike with Matchers {
  private val objectMapper = JacksonObjectMapperExtension(system).objectMapperJson
  test("OrgType") {
    println(OrgTypes.values)
    println(OrgTypes.values.map(v => s"{index:${v.index}, value:${v.getValue}, name:${v.getName}}"))
    println(objectMapper.writeValueAsString(OrgTypes.valueNames))

    val jsonstring = objectMapper.writeValueAsString(OrgTypes.values)
    println(jsonstring)
    val orgType = objectMapper.readValue("1", classOf[OrgType])
    println(orgType)

    orgType match {
      case OrgTypes.COMPANY => println("company")
      case OrgTypes.SCHOOL  => println("school")
    }
  }

  test("UserType") {
    println(UserType.values().toVector)
    println(UserType.values().toVector.map(_.toValueName))
    println(objectMapper.writeValueAsString(UserType.values().toVector.map(_.toValueName)))

    val jsonstring = objectMapper.writeValueAsString(UserType.values())
    println(jsonstring)

    val userType = objectMapper.readValue("1", classOf[UserType])
    println(userType)
    userType shouldBe UserType.NORMAL

    userType match {
      case UserType.NORMAL => println("normal")
    }

    val userTypes = objectMapper.readValue(jsonstring, classOf[Vector[UserType]])
    println(userTypes)
  }
}
