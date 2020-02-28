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

package fusion.discovery.http

import akka.http.scaladsl.model.Uri
import fusion.testkit.FusionFunSuiteLike

class NacosHttpClientTest extends FusionFunSuiteLike {
  test("testUrl") {
    val uri = Uri("http://fusion-server-account/api/v4/account/credential/login")
    println(uri.authority.host.address())
  }
}
