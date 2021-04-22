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

package fusion.sbt

import fusion.sbt.gen.BuildInfo
import sbt._

object FusionImport {
  val fusionActuator = component("fusion-actuator")
  val fusionActuatorCluster = component("fusion-actuator-cluster")
  val fusionCassandra = component("fusion-cassandra")
  val fusionCluster = component("fusion-cluster")
  val fusionCore = component("fusion-core")
  val fusionDiscoveryClient = component("fusion-discovery-client")
  val fusionDoc = component("fusion-doc")
  val fusionElasticsearch = component("fusion-elasticsearch")
  val fusionHttp = component("fusion-http")
  val fusionHttpClient = component("fusion-http-client")
  val fusionHttpGateway = component("fusion-http-gateway")
  val fusionInject = component("fusion-inject")
  val fusionInjectGuice = component("fusion-inject-guice")
  val fusionInjectGuiceTestkit = component("fusion-inject-guice-testkit")
  val fusionJdbc = component("fusion-jdbc")
  val fusionJob = component("fusion-job")
  val fusionJsonJackson = component("fusion-json-jackson")
  val fusionJsonJacksonExt = component("fusion-json-jackson-ext")
  val fusionKafka = component("fusion-kafka")
  val fusionLog = component("fusion-log")
  val fusionMail = component("fusion-mail")
  val fusionMongodb = component("fusion-mongodb")
  val fusionMq = component("fusion-mq")
  val fusionMybatis = component("fusion-mybatis")
  val fusionOauth = component("fusion-oauth")
  val fusionProtobufV3 = component("fusion-protobuf-v3")
  val fusionSecurity = component("fusion-security")
  val fusionSlick = component("fusion-slick")
  val fusionTestkit = component("fusion-testkit")
  val helloscalaCommon = component("helloscala-common")

  def component(id: String) = "com.helloscala.fusion" %% id % BuildInfo.version
}
