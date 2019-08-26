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

package fusion.common.constant

object ConfigKeys {

  val AKKA_MANAGEMENT_FUSION = "akka.management.fusion"
  val AKKA_MANAGEMENT_FUSION_ENABLE = "akka.management.fusion.enable"

  object FUSION {
    val PIDFILE: String = "fusion.pidfile"
  }

  object HTTP {
    val CUSTOM_MEDIA_TYPES = "fusion.http.custom-media-types"
  }
}
