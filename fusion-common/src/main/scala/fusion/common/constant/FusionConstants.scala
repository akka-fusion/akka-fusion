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

package fusion.common.constant

object FusionConstants {
  val X_SERVER = "X-Server"
  val X_TRACE_NAME = "X-Trace-Id"
  val X_REQUEST_TIME = "X-Request-Time"
  val X_SPAN_TIME = "X-Span-Time"
  val HEADER_NAME = "Fusion-Server"
  val FUSION: String = "fusion"
  val NAME_PATH = s"$FUSION.name"
  val AKKA_NAME_PATH = s"$FUSION.akka-name"
  val SERVER_HOST_PATH = s"$FUSION.server.host"
  val SERVER_PORT_PATH = s"$FUSION.server.port"
  val SERVICE_NAME_PATH = s"$FUSION.service.name"
  val PROFILES_ACTIVE_PATH = s"$FUSION.profiles.active"
  val GLOBAL_APPLICATION_ENABLE = s"$FUSION.global-application-enable"
}
