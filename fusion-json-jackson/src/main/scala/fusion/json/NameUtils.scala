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

package fusion.json

object NameUtils {

  def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit =
      if (name.nonEmpty) {
        val (r, capNext2) = name.head match {
          case c if c.isLower => (Some(if (capNext) c.toUpper else c), false)
          case c if c.isUpper =>
            // force first letter to lower unless forced to capitalize it.
            (Some(if (index == 0 && !capNext) c.toLower else c), false)
          case c if c.isDigit => (Some(c), true)
          case _              => (None, true)
        }
        r.foreach(b.append)
        inner(name.tail, index + 1, capNext2)
      }
    inner(name, 0, upperInitial)
    b.toString
  }
}
