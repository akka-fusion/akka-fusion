/*
 * Copyright 2019 akka-fusion.com
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

package helloscala.common.util

import com.fasterxml.jackson.annotation.{ JsonIgnore, JsonValue }
import helloscala.common.data.IntValueName

trait EnumTrait {
  def companion: EnumTraitCompanion

  @JsonIgnore @transient final val index: Int = companion.generateId()

  protected val name: String = StringUtils.dropLast$(this.getClass.getSimpleName.toLowerCase)
  protected val value = index

  def getName: String = name
  @JsonValue def getValue: Int = value

  def toValueName: IntValueName = IntValueName(getValue, getName)
}

//abstract class BaseEnum(override val companion: EnumTraitCompanion[_], override protected val value: Int)
//    extends EnumTrait

trait EnumTraitCompanion {
  type Value <: EnumTrait

  private var _index = 0
  private var _values = Vector[Value]()

  final def values: Vector[Value] = _values

  final def valueNames: Vector[IntValueName] = values.map(_.toValueName)

  protected def registerEnums(e: Value*): Unit = {
    _values = e.sortWith(_.getValue < _.getValue).toVector
  }

  final def generateId(): Int = {
    val id = _index
    _index += 1
    id
  }

  final def fromValue(value: Int): Option[Value] = values.find(_.getValue == value)
  final def fromName(name: String): Option[Value] = values.find(_.getName == name)
}
