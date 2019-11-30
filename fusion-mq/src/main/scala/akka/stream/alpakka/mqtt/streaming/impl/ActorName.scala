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

package akka.stream.alpakka.mqtt.streaming
package impl

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

import akka.annotation.InternalApi

/*
 * Provides the ability to form valid actor names
 */
@InternalApi object ActorName {
  private val Utf8 = StandardCharsets.UTF_8.name()

  def mkName(name: String): String =
    URLEncoder.encode(name, Utf8)
}
