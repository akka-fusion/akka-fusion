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

package fusion.sbt

import sbt._
import fusion.sbt.gen.BuildInfo

object FusionImport {
  val helloscalaCommon = component("helloscala-common")
  val fusionCommon = component("fusion-common")
  val fusionProtobufV3 = component("fusion-protobuf-v3")
  val fusionCore = component("fusion-core")
  val fusionTestkit = component("fusion-testkit")
  val fusionSecurity = component("fusion-security")
  val fusionMail = component("fusion-mail")
  val fusionDoc = component("fusion-doc")
  val fusionJdbc = component("fusion-jdbc")
  val fusionMybatis = component("fusion-mybatis")
  val fusionSlick = component("fusion-slick")
  val fusionKafka = component("fusion-kafka")
  val fusionElasticsearch = component("fusion-elasticsearch")
  val fusionCassandra = component("fusion-cassandra")
  val fusionLog = component("fusion-log")
  val fusionJson = component("fusion-json")
  val fusionJsonCirce = component("fusion-json-circe")
  val fusionHttpClient = component("fusion-http-client")
  val fusionHttp = component("fusion-http")
  val fusionBoot = component("fusion-boot")
  val fusionMongodb = component("fusion-mongodb")
  val fusionOauth = component("fusion-oauth")
  val fusionJob = component("fusion-job")
  val fusionActuator = component("fusion-actuator")
  val fusionActuatorCluster = component("fusion-actuator-cluster")
  val fusionDiscoveryClient = component("fusion-discovery-client")
  val fusionHttpGateway = component("fusion-http-gateway")

  def component(id: String) = "com.akka-fusion" %% id % BuildInfo.version
}
