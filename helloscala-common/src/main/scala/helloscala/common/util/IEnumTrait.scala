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

package helloscala.common.util

import helloscala.common.data.ValueName

trait IEnumTrait[V] {
  self =>
  //  val companion: IEnumTraitCompanion[V]
  val name: String = StringUtils.dropLast$(this.getClass.getSimpleName)
  val value: V

  def toValueName: ValueName[V] =
    new ValueName[V] {
      override def value: V = self.value
      override def name: String = self.name
    }
}

trait StringEnumTrait extends IEnumTrait[String] {
  override val value: String = name
}

trait IEnumTraitCompanion[V] {
  type Value <: IEnumTrait[V]
  val values: Vector[Value]
  def optionFromValue(value: String): Option[Value] = values.find(_.value == value)

  final def fromValue(value: String): Value =
    optionFromValue(value).getOrElse(
      throw new NoSuchElementException(s"${getClass.getSimpleName}.values by value not found, it is $value.")
    )
  def optionFromName(name: String): Option[Value] = values.find(_.name == name)

  final def fromName(name: String): Value =
    optionFromName(name).getOrElse(
      throw new NoSuchElementException(s"${getClass.getSimpleName}.values by name not found, it is $name.")
    )
}
