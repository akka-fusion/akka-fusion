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

package fusion.discoveryx.server

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ Directive, PathMatcher }

package object route {
  def pathPost[L](pm: PathMatcher[L]): Directive[L] = path(pm) & post
  def pathGet[L](pm: PathMatcher[L]): Directive[L] = path(pm) & get
  def pathPut[L](pm: PathMatcher[L]): Directive[L] = path(pm) & put
  def pathDelete[L](pm: PathMatcher[L]): Directive[L] = path(pm) & delete
}
