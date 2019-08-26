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

package fusion.core.model

trait HealthComponent {

  def name: String = {
    val s = getClass.getSimpleName
    val str = s.head.toLower + s.tail
    if (str.last == '$') str.init else str
  }

  def health: Health
}

case class Health(status: String, details: Map[String, Any])

object Health {
  val UP = "UP"
  val DOWN = "DOWN"

  def up(details: Map[String, Any]): Health = Health(UP, details)
  def up(): Health = Health(UP, null)
  def up(detail: (String, Any), details: (String, Any)*): Health = up(Map(detail) ++ details)
  def down(details: Map[String, Any]): Health = Health(DOWN, details)
  def down(): Health = Health(DOWN, null)
  def down(detail: (String, Any), details: (String, Any)*): Health = down(Map(detail) ++ details)
}
