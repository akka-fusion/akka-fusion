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

package fusion.core.extension

import helloscala.common.Configuration

class RunMode(configuration: Configuration) {

  val active: String = configuration
    .get[Option[String]]("fusion.profiles.active")
    .orElse(configuration.get[Option[String]]("spring.profiles.active"))
    .getOrElse("dev")

  def isDev: Boolean = active == "dev"
  def isProd: Boolean = active == "prod"
  def isTest: Boolean = active == "test"
  def isIt: Boolean = active == "it" || isTest
}
