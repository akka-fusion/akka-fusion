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

///*
// * Copyright 2019-2021 helloscala.com
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fusion.cloud.discovery.client.nacos
//
//import akka.discovery.{Discovery, Lookup}
//import akka.testkit.TestKit
//import akka.{actor => classic}
//import fusion.testkit.FusionFunSuiteLike
//
//import scala.concurrent.Await
//import scala.concurrent.duration._
//
//class AkkaDiscoveryTest extends TestKit(classic.ActorSystem()) with FusionFunSuiteLike {
//  test("discovery") {
//    val discovery = Discovery(system).discovery
//    val resolvedF = discovery.lookup(Lookup("service1"), 10.seconds)
//    val resolved = Await.result(resolvedF, 10.seconds)
//    println(resolved)
//  }
//}
