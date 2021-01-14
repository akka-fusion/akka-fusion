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

package fusion.common.constant

object FusionKeys {
  val FUSION_NAME = "fusion.name"
  val AKKA_NAME = "fusion.akka.name"
  val PIDFILE = "fusion.pidfile"
  val GLOBAL_APPLICATION_ENABLE = "fusion.global-application-enable"
  val PROFILES_ACTIVE_PATH = "fusion.profiles.active"

  object CORE {
    val CIRCUIT_BREAKER = "fusion.core.circuit-breaker"
  }

  object HTTP {
    val CUSTOM_MEDIA_TYPES = "fusion.http.custom-media-type"
  }
}
