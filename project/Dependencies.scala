import sbt._

object Dependencies {
  val versionScala212 = "2.12.10"
  val versionScala213 = "2.13.1"
  val versionScalaXml = "1.2.0"
  val versionScalaCollectionCompat = "2.1.4"
  val versionJava8Compat = "0.9.0"
  val versionScalameta = "4.2.5"
  val versionScalatest = "3.1.0"
  val versionAkka = "2.6.3"
  val versionAkkaManagement = "1.0.5"
  val versionAkkaHttp = "10.1.11"
  val versionAkkaHttpCors = "0.4.2"
  val versionAlpakka = "2.0.0-M2"
  val versionAlpakkaKafka = "2.0.1"
  val versionCassandra = "4.3.1"
  val versionJackson = "2.10.2"
  val versionElastic4s = "7.3.5"
  val versionConfig = "1.4.0"
  val versionPureconfig = "0.12.2"
  val versionGuice = "4.2.2"
  val versionKamon = "2.0.4"
  val versionKanela = "1.0.4"
  val versionUuidGenerator = "3.2.0"
  val versionHanlp = "portable-1.7.6"
  val versionSlick = "3.3.2"
  val versionSlickPg = "0.18.1"
  val versionPoi = "4.1.1"
  val versionQuartz = "2.3.2"
  val versionBcprovJdk15on = "1.64"
  val versionNacos = "1.1.4"
  val versionJsch = "0.1.55"
  val versionJakartaMail = "1.6.4"
  val versionHikariCP = "3.4.1"
  val versionMybatisPlus = "3.3.0"
  val versionLombok = "1.18.12"
  val versionMySQL = "8.0.19"
  val versionPostgres = "42.2.10"
  val versionRequests = "0.5.1"
  val versionFastparse = "2.2.4"
  val versionOsLib = "0.6.3"
  val versionMongoScalaBson = "2.8.0"
  val versionMongoDriverReactivestreams = "1.13.0"
  val versionBson = "3.12.0"
  val versionKafka = "2.4.0"
  val versionAlpnAgent = "2.0.9"
  val versionLogback = "1.2.3"
  val versionScalaLogging = "3.9.2"
  val versionLogstashLogback = "6.3"
  val versionJwt = "4.2.0"
  val versionJson4s = "3.6.7"
  val versionScalapbJson4s = "0.10.0"

  val _scalameta = "org.scalameta" %% "scalameta" % versionScalameta
  val _scalaXml = ("org.scala-lang.modules" %% "scala-xml" % versionScalaXml).exclude("org.scala-lang", "scala-library")

  val _scalaJava8Compat =
    ("org.scala-lang.modules" %% "scala-java8-compat" % versionJava8Compat).exclude("org.scala-lang", "scala-library")
  val _scalaCollectionCompat = "org.scala-lang.modules" %% "scala-collection-compat" % versionScalaCollectionCompat

  val _scalatest = "org.scalatest" %% "scalatest" % versionScalatest
  val _akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % versionAkka
  val _akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % versionAkka
  val _akkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % versionAkka
  val _akkaSerializationJackson = "com.typesafe.akka" %% "akka-serialization-jackson" % versionAkka
  val _akkaProtobufV3 = "com.typesafe.akka" %% "akka-protobuf-v3" % versionAkka
  val _akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed" % versionAkka
  val _akkaTypedTestkit = "com.typesafe.akka" %% "akka-actor-testkit-typed" % versionAkka
  val _akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % versionAkka

  val _akkas =
    Seq("com.typesafe.akka" %% "akka-slf4j" % versionAkka, _akkaActorTyped, _akkaStreamTyped)
      .map(_.exclude("org.scala-lang.modules", "scala-java8-compat").cross(CrossVersion.binary))
  val _akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % versionAkka

  val _akkaClusters = Seq(
    "com.typesafe.akka" %% "akka-cluster-typed" % versionAkka,
    "com.typesafe.akka" %% "akka-cluster-sharding-typed" % versionAkka)

  val _akkaManagement =
    ("com.lightbend.akka.management" %% "akka-management" % versionAkkaManagement)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)
      .exclude("org.scala-lang", "scala-library")

  val _akkaManagementClusterHttp =
    ("com.lightbend.akka.management" %% "akka-management-cluster-http" % versionAkkaManagement)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)
      .exclude("org.scala-lang", "scala-library")

  val _akkaHttp = ("com.typesafe.akka" %% "akka-http" % versionAkkaHttp)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttpTestkit = ("com.typesafe.akka" %% "akka-http-testkit" % versionAkkaHttp)
    .exclude("com.typesafe.akka", "akka-stream-testkit")
    .cross(CrossVersion.binary)
    .exclude("com.typesafe.akka", "akka-testkit")
    .cross(CrossVersion.binary)

  val _akkaHttpCors = ("ch.megard" %% "akka-http-cors" % versionAkkaHttpCors)
    .excludeAll(ExclusionRule("com.typesafe.akka"))
    .cross(CrossVersion.binary)

  val _akkaHttp2 = ("com.typesafe.akka" %% "akka-http2-support" % versionAkkaHttp)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttps = Seq(_akkaHttp, _akkaHttp2, _akkaHttpTestkit % Test)

  val _akkaGrpcRuntime = ("com.lightbend.akka.grpc" %% "akka-grpc-runtime" % akka.grpc.gen.BuildInfo.version)
    .exclude("com.typesafe", "config")
    .exclude("com.typesafe", "ssl-config-core")
    .cross(CrossVersion.binary)
    .excludeAll(ExclusionRule("com.typesafe.akka"))
    .cross(CrossVersion.binary)

  val _alpakkaSimpleCodecs =
    ("com.lightbend.akka" %% "akka-stream-alpakka-simple-codecs" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaXml =
    ("com.lightbend.akka" %% "akka-stream-alpakka-xml" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaCsv =
    ("com.lightbend.akka" %% "akka-stream-alpakka-csv" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaJsonStreaming =
    ("com.lightbend.akka" %% "akka-stream-alpakka-json-streaming" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaFile =
    ("com.lightbend.akka" %% "akka-stream-alpakka-file" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaFtp =
    ("com.lightbend.akka" %% "akka-stream-alpakka-ftp" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaUnixDomainSocket =
    ("com.lightbend.akka" %% "akka-stream-alpakka-unix-domain-socket" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaMqttStreaming =
    ("com.lightbend.akka" %% "akka-stream-alpakka-mqtt-streaming" % versionAlpakka)
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
    "org.mongodb.scala" %% "mongo-scala-bson" % versionMongoScalaBson,
    "org.mongodb" % "mongodb-driver-reactivestreams" % versionMongoDriverReactivestreams)

  val _jacksonAnnotations = "com.fasterxml.jackson.core" % "jackson-annotations" % versionJackson

  val _cassandras = Seq("com.datastax.oss" % "java-driver-core" % versionCassandra)

  val _elastic4ses = Seq(
    "com.sksamuel.elastic4s" %% "elastic4s-core" % versionElastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % versionElastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % versionElastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-json-jackson" % versionElastic4s,
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % versionElastic4s % Test)

  val _alpakkaText =
    ("com.lightbend.akka" %% "akka-stream-alpakka-text" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _akkaStreamKafkas = Seq(
    ("com.typesafe.akka" %% "akka-stream-kafka" % versionAlpakkaKafka)
      .exclude("com.typesafe.akka", "akka-stream")
      .cross(CrossVersion.binary))

  val _config = "com.typesafe" % "config" % versionConfig

//  val _pureconfig = "com.github.pureconfig" %% "pureconfig" % versionPureconfig

  val _hanlp = "com.hankcs" % "hanlp" % versionHanlp
  val _uuidGenerator = ("com.fasterxml.uuid" % "java-uuid-generator" % versionUuidGenerator).exclude("log4j", "log4j")
  val _guice = "com.google.inject" % "guice" % versionGuice

  val _json4s =
    ("org.json4s" %% "json4s-jackson" % versionJson4s).exclude("com.fasterxml.jackson.core", "jackson-databind")

  val _scalapbJson4s = "com.thesamet.scalapb" %% "scalapb-json4s" % versionScalapbJson4s

  val _circeGeneric = "io.circe" %% "circe-generic" % "0.12.2"

  val _scalapbCirce = "io.github.scalapb-json" %% "scalapb-circe" % "0.5.1"

  val _kanelaAgent = "io.kamon" % "kanela-agent" % versionKanela % Provided

  val _kamonStatusPage = "io.kamon" %% "kamon-status-page" % versionKamon

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
  val _osLib = "com.lihaoyi" %% "os-lib" % versionOsLib
  val _requests = "com.lihaoyi" %% "requests" % versionRequests
  val _fastparse = "com.lihaoyi" %% "fastparse" % versionFastparse

  val _jwt = "com.pauldijou" %% "jwt-core" % versionJwt
  val _jwtJson4s = ("com.pauldijou" %% "jwt-json4s-jackson" % versionJwt).excludeAll(ExclusionRule("org.json4s"))

  val _slicks =
    Seq("com.typesafe.slick" %% "slick" % versionSlick, "com.typesafe.slick" %% "slick-testkit" % versionSlick % Test)
  val _slickPg = "com.github.tminglei" %% "slick-pg" % versionSlickPg
  val _pois = Seq("org.apache.poi" % "poi-scratchpad" % versionPoi, "org.apache.poi" % "poi-ooxml" % versionPoi)
  val _scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % versionScalaLogging
  val _logbackClassic = "ch.qos.logback" % "logback-classic" % versionLogback
  val _logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % versionLogstashLogback
  val _bcprovJdk15on = "org.bouncycastle" % "bcprov-jdk15on" % versionBcprovJdk15on
  val _quartz = ("org.quartz-scheduler" % "quartz" % versionQuartz).exclude("com.zaxxer", "HikariCP-java7")
  val _mybatisPlus = "com.baomidou" % "mybatis-plus" % versionMybatisPlus
  val _lombok = "org.projectlombok" % "lombok" % versionLombok
  val _postgresql = "org.postgresql" % "postgresql" % versionPostgres
  val _mysql = "mysql" % "mysql-connector-java" % versionMySQL
  val _h2 = "com.h2database" % "h2" % "1.4.200"
  val _hikariCP = "com.zaxxer" % "HikariCP" % versionHikariCP
  val _jsch = "com.jcraft" % "jsch" % versionJsch
  val _nacosClient = "com.alibaba.nacos" % "nacos-client" % versionNacos
  val _jakartaMail = "com.sun.mail" % "jakarta.mail" % versionJakartaMail
  val _bson = "org.mongodb" % "bson" % versionBson
  val _mssql = "com.microsoft.sqlserver" % "mssql-jdbc" % "6.4.0.jre8"
  val _commonsVfs = "org.apache.commons" % "commons-vfs2" % "2.2"
  val _kafkaClients = "org.apache.kafka" % "kafka-clients" % versionKafka
  val _alpnAgent = "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % versionAlpnAgent
}
