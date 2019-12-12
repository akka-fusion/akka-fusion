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

///*
// * Copyright 2019 helloscala.com
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
//package fusion.schedulerx
//
//import akka.actor.AddressFromURIString
//import com.typesafe.config.{ Config, ConfigFactory }
//
//import scala.jdk.CollectionConverters._
//
//object SchedulerXConfigFactory {
//  def arrangeConfig(originalConfig: Config): Config = {
//    val c = ConfigFactory
//      .parseString(s"akka ${originalConfig.getConfig(s"${Constants.SCHEDULERX}.akka").root().render()}")
//      .withFallback(originalConfig)
//    val name = c.getString(s"${Constants.SCHEDULERX}.name")
//    val seedNodes = c
//      .getStringList("akka.cluster.seed-nodes")
//      .asScala
//      .map {
//        case addr if !addr.startsWith("akka://") =>
//          val address = AddressFromURIString.parse(s"akka://$name@$addr")
//          require(
//            address.system == name,
//            s"Cluster ActorSystem name must equals be $name, but seed-node name is invalid, it si $addr.")
//          address
//        case addr => AddressFromURIString.parse(addr)
//      }
//      .toList
//    ConfigFactory
//      .parseString(s"""akka.cluster.seed-nodes = ${seedNodes.mkString("[\"", "\", \"", "\"]")}""".stripMargin)
//      .withFallback(c)
//  }
//}
