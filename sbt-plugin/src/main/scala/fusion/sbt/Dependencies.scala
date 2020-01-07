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

object Dependencies {
  object Version {
    val scala212 = "2.12.10"
    val scala213 = "2.13.1"
    val scalaXml = "1.2.0"
    val scalaCollectionCompat = "2.1.3"
    val java8Compat = "0.9.0"
    val scalameta = "4.2.5"
    val scalatest = "3.0.8"
    val akka = "2.6.1"
    val akkaManagement = "1.0.5"
    val akkaHttp = "10.1.11"
    val akkaHttpCors = "0.4.2"
    val alpakka = "2.0.0-M2"
    val alpakkaKafka = "2.0.0-RC1"
    val cassandra = "4.3.1"
    val elastic4s = "6.7.4"
    val config = "1.4.0"
    val pureconfig = "0.12.2"
    val guice = "4.2.2"
    val kamon = "2.0.4"
    val kanela = "1.0.4"
    val uuidGenerator = "3.2.0"
    val hanlp = "portable-1.7.6"
    val slick = "3.3.2"
    val slickPg = "0.18.1"
    val poi = "4.1.1"
    val quartz = "2.3.2"
    val bcprovJdk15on = "1.64"
    val nacos = "1.1.4"
    val jsch = "0.1.55"
    val jakartaMail = "1.6.4"
    val hikariCP = "3.4.1"
    val mybatisPlus = "3.3.0"
    val lombok = "1.18.10"
    val mySQL = "8.0.18"
    val postgres = "42.2.9"
    val requests = "0.4.7"
    val fastparse = "2.2.2"
    val osLib = "0.6.2"
    val mongoScalaBson = "2.8.0"
    val mongoDriverReactivestreams = "1.13.0"
    val bson = "3.12.0"
    val kafka = "2.4.0"
    val alpnAgent = "2.0.9"
    val logback = "1.2.3"
    val scalaLogging = "3.9.2"
    val logstashLogback = "6.3"
    val jwt = "4.2.0"
    val json4s = "3.6.7"
    val scalapbJson4s = "0.10.0"
  }

  val _scalameta = "org.scalameta" %% "scalameta" % Version.scalameta
  val _scalaXml =
    ("org.scala-lang.modules" %% "scala-xml" % Version.scalaXml).exclude("org.scala-lang", "scala-library")

  val _scalaJava8Compat =
    ("org.scala-lang.modules" %% "scala-java8-compat" % Version.java8Compat).exclude("org.scala-lang", "scala-library")
  val _scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % Version.scalaCollectionCompat

  val _scalatest = "org.scalatest" %% "scalatest" % Version.scalatest
  val _akkaActor = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val _akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka
  val _akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val _akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % Version.akka
  val _akkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % Version.akka
  val _akkaSerializationJackson = "com.typesafe.akka" %% "akka-serialization-jackson" % Version.akka
  val _akkaProtobufV3 = "com.typesafe.akka" %% "akka-protobuf-v3" % Version.akka
  val _akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed" % Version.akka
  val _akkaTypedTestkit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Version.akka
  val _akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % Version.akka

  val _akkas =
    Seq("com.typesafe.akka" %% "akka-slf4j" % Version.akka, _akkaActorTyped, _akkaStreamTyped)
      .map(_.exclude("org.scala-lang.modules", "scala-java8-compat").cross(CrossVersion.binary))
  val _akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % Version.akka

  val _akkaClusters = Seq(
    "com.typesafe.akka" %% "akka-cluster-typed" % Version.akka,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Version.akka)

  val _akkaManagement =
    ("com.lightbend.akka.management" %% "akka-management" % Version.akkaManagement)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)
      .exclude("org.scala-lang", "scala-library")

  val _akkaManagementClusterHttp =
    ("com.lightbend.akka.management" %% "akka-management-cluster-http" % Version.akkaManagement)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)
      .exclude("org.scala-lang", "scala-library")

  val _akkaHttp = ("com.typesafe.akka" %% "akka-http" % Version.akkaHttp)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttpTestkit = ("com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp)
    .exclude("com.typesafe.akka", "akka-stream-testkit")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-testkit")
    .cross(CrossVersion.binary)

  val _akkaHttpCors = ("ch.megard" %% "akka-http-cors" % Version.akkaHttpCors)
    .excludeAll(ExclusionRule("com.typesafe.akka"))
    .cross(CrossVersion.binary)

  val _akkaHttp2 = ("com.typesafe.akka" %% "akka-http2-support" % Version.akkaHttp)
    .exclude("com.typesafe.akka", "akka-http-core")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttps = Seq(_akkaHttp, _akkaHttp2, _akkaHttpTestkit % Test)

//  val _akkaGrpcRuntime = ("com.lightbend.akka.grpc" %% "akka-grpc-runtime" % akka.grpc.gen.BuildInfo.version)
//    .exclude("com.typesafe", "config")
//    .exclude("com.typesafe", "ssl-config-core")
//    .cross(CrossVersion.binary)
//    .excludeAll(ExclusionRule("com.typesafe.akka"))
//    .cross(CrossVersion.binary)

  val _alpakkaSimpleCodecs =
    ("com.lightbend.akka" %% "akka-stream-alpakka-simple-codecs" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaXml =
    ("com.lightbend.akka" %% "akka-stream-alpakka-xml" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaCsv =
    ("com.lightbend.akka" %% "akka-stream-alpakka-csv" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaJsonStreaming =
    ("com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaFile =
    ("com.lightbend.akka" %% "akka-stream-alpakka-file" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaFtp =
    ("com.lightbend.akka" %% "akka-stream-alpakka-ftp" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaUnixDomainSocket =
    ("com.lightbend.akka" %% "akka-stream-alpakka-unix-domain-socket" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaMqttStreaming =
    ("com.lightbend.akka" %% "akka-stream-alpakka-mqtt-streaming" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _akkaPersistenceCassandra =
    ("com.typesafe.akka" %% "akka-persistence-cassandra" % "0.100")
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _akkaPersistenceJdbc =
    ("com.github.dnvriend" %% "akka-persistence-jdbc" % "3.5.2")
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _mongodbs = Seq(
    "org.mongodb.scala" %% "mongo-scala-bson" % Version.mongoScalaBson,
    "org.mongodb" % "mongodb-driver-reactivestreams" % Version.mongoDriverReactivestreams)

  val _jacksonAnnotations = "com.fasterxml.jackson.core" % "jackson-annotations" % "2.10.1"

  val _cassandras = Seq("com.datastax.oss" % "java-driver-core" % Version.cassandra)

  val _elastic4ses = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % Version.elastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % Version.elastic4s,
    ("com.sksamuel.elastic4s" %% "elastic4s-json4s" % Version.elastic4s).excludeAll(ExclusionRule("org.json4s")),
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % Version.elastic4s % Test)

  val _alpakkaElasticsearch =
    ("com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaText =
    ("com.lightbend.akka" %% "akka-stream-alpakka-text" % Version.alpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _akkaStreamKafkas = Seq(
    ("com.typesafe.akka" %% "akka-stream-kafka" % Version.alpakkaKafka)
      .exclude("com.typesafe.akka", "akka-stream")
      .cross(CrossVersion.binary))

  val _config = "com.typesafe" % "config" % Version.config

//  val _pureconfig = "com.github.pureconfig" %% "pureconfig" % versionPureconfig

  val _hanlp = "com.hankcs" % "hanlp" % Version.hanlp
  val _uuidGenerator = ("com.fasterxml.uuid" % "java-uuid-generator" % Version.uuidGenerator).exclude("log4j", "log4j")
  val _guice = "com.google.inject" % "guice" % Version.guice

  val _json4s =
    ("org.json4s" %% "json4s-jackson" % Version.json4s).exclude("com.fasterxml.jackson.core", "jackson-databind")

  val _scalapbJson4s = "com.thesamet.scalapb" %% "scalapb-json4s" % Version.scalapbJson4s

  val _circeGeneric = "io.circe" %% "circe-generic" % "0.12.2"

  val _scalapbCirce = "io.github.scalapb-json" %% "scalapb-circe" % "0.5.1"

  val _kanelaAgent = "io.kamon" % "kanela-agent" % Version.kanela % Provided

  val _kamonStatusPage = "io.kamon" %% "kamon-status-page" % Version.kamon

  val _kamonAkka = ("io.kamon" %% "kamon-akka" % "2.0.1")
    .excludeAll("com.typesafe.akka")
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

  val _kamonAkkaHttp = ("io.kamon" %% "kamon-akka-http" % "2.0.3")
    .exclude("io.kamon", "kamon-akka-2.5")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-http")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "config")
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

  // need aspectjweaver
  val _kamonAkkaRemote = ("io.kamon" %% "kamon-akka-remote" % "2.0.0-RC2")
    .exclude("io.kamon", "kamon-akka-2.5")
    .cross(CrossVersion.binary)
    .excludeAll(ExclusionRule("com.typesafe.akka"))
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

//  val _oshiCore = "com.github.oshi" % "oshi-core" % "4.3.0"

  val _kamonSystemMetrics =
    ("io.kamon" %% "kamon-system-metrics" % "2.0.1").exclude("org.scala-lang", "scala-library")
  val _kamonPrometheus = "io.kamon" %% "kamon-prometheus" % "2.0.1"
  val _kamonZipkin = "io.kamon" %% "kamon-zipkin" % "2.0.0"
  val _kamonLogback = "io.kamon" %% "kamon-logback" % "2.0.2"
  val _osLib = "com.lihaoyi" %% "os-lib" % Version.osLib
  val _requests = "com.lihaoyi" %% "requests" % Version.requests
  val _fastparse = "com.lihaoyi" %% "fastparse" % Version.fastparse

  val _jwt = "com.pauldijou" %% "jwt-core" % Version.jwt
  val _jwtJson4s = ("com.pauldijou" %% "jwt-json4s-jackson" % Version.jwt).excludeAll(ExclusionRule("org.json4s"))

  val _slicks =
    Seq("com.typesafe.slick" %% "slick" % Version.slick, "com.typesafe.slick" %% "slick-testkit" % Version.slick % Test)
  val _slickPg = "com.github.tminglei" %% "slick-pg" % Version.slickPg

  val _pois = Seq("org.apache.poi" % "poi-scratchpad" % Version.poi, "org.apache.poi" % "poi-ooxml" % Version.poi)

  val _logs = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % Version.scalaLogging,
    "ch.qos.logback" % "logback-classic" % Version.logback)

  val _logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % Version.logstashLogback

  val _bcprovJdk15on = "org.bouncycastle" % "bcprov-jdk15on" % Version.bcprovJdk15on
  val _quartz = ("org.quartz-scheduler" % "quartz" % Version.quartz).exclude("com.zaxxer", "HikariCP-java7")
  val _mybatisPlus = "com.baomidou" % "mybatis-plus" % Version.mybatisPlus
  val _lombok = "org.projectlombok" % "lombok" % Version.lombok
  val _postgresql = "org.postgresql" % "postgresql" % Version.postgres
  val _mysql = "mysql" % "mysql-connector-java" % Version.mySQL
  val _h2 = "com.h2database" % "h2" % "1.4.200"
  val _hikariCP = "com.zaxxer" % "HikariCP" % Version.hikariCP
  val _jsch = "com.jcraft" % "jsch" % Version.jsch
  val _nacosClient = "com.alibaba.nacos" % "nacos-client" % Version.nacos
  val _jakartaMail = "com.sun.mail" % "jakarta.mail" % Version.jakartaMail
  val _bson = "org.mongodb" % "bson" % Version.bson
  val _mssql = "com.microsoft.sqlserver" % "mssql-jdbc" % "6.4.0.jre8"
  val _commonsVfs = "org.apache.commons" % "commons-vfs2" % "2.2"
  val _kafkaClients = "org.apache.kafka" % "kafka-clients" % Version.kafka
  val _alpnAgent = "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % Version.alpnAgent
}
