import sbt._

object Dependencies {
  val versionScala212 = "2.12.12"
  val versionScala213 = "2.13.6"
  val versionScalaXml = "1.3.0"
  val versionScalaCollectionCompat = "2.4.3"
  val versionJava8Compat = "0.9.1"
  val versionScalameta = "4.4.10"
  val versionScalatest = "3.1.4"
  val versionAkka: String = sys.props.getOrElse("akka.version", "2.6.14")
  val versionAkkaManagement = "1.1.0"
  val versionAkkaHttp = "10.2.4"
  val versionAkkaHttpCors = "1.1.1"
  val versionAlpakka = "3.0.0"
  val versionAlpakkaKafka = "2.1.0"
  val versionAkkaPersistenceCassandra = "1.0.5"
  val versionAkkaPersistenceJdbc = "5.0.1"
  val versionCassandra = "4.11.0"
  val versionJackson = "2.11.4"
  val versionElastic4s = "7.12.0"
  val versionConfig = "1.4.1"
  val versionPureconfig = "0.15.0"
  val versionChimney = "0.6.1"
  val versionGuice = "5.0.1"
  val versionUuidGenerator = "4.0.1"
  val versionHanlp = "portable-1.7.8"
  val versionSlick = "3.3.3"
  val versionSlickPg = "0.19.5"
  val versionPoi = "4.1.2"
  val versionQuartz = "2.3.2"
  val versionBcprovJdk15on = "1.68"
  val versionJsch = "0.1.55"
  val versionJakartaMail = "2.0.1"
  val versionHikariCP = "4.0.3"
  val versionMybatisPlus = "3.4.2"
  val versionSlf4j = "1.7.30"
  val versionLombok = "1.18.20"
  val versionMySQL = "8.0.24"
  val versionH2 = "1.4.200"
  val versionMssqlJdbc = "9.2.1.jre11"
  val versionPostgres = "42.2.19"
  val versionCommonsVfs2 = "2.8"
  val versionRequests = "0.6.7"
  val versionFastparse = "2.3.2"
  val versionOsLib = "0.7.4"
  val versionMongoScalaBson = "4.2.3"
  val versionMongoDriverReactivestreams = "4.2.3"
  val versionBson = "4.2.3"
  val versionKafka = "2.6.1"
  val versionAlpnAgent = "2.0.10"
  val versionLogback = "1.2.3"
  val versionScalaLogging = "3.9.3"
  val versionLogstashLogback = "6.6"
  val versionJwt = "5.0.0"
  val versionScalapbJson4s = "0.11.0"
  val versionJoseJwt = "9.8.1"
  val versionPulsar4sAkkaStreams = "2.7.2"
  val versionPulsar = "2.7.1"

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

  val _akkaHttpCors = ("ch.megard" %% "akka-http-cors" % versionAkkaHttpCors)
    .excludeAll(ExclusionRule("com.typesafe.akka"))
    .cross(CrossVersion.binary)

  val _akkaHttp2 = ("com.typesafe.akka" %% "akka-http2-support" % versionAkkaHttp)
    .exclude("com.typesafe.akka", "akka-stream")
    .cross(CrossVersion.binary)

  val _akkaHttps = Seq(
    _akkaHttpCors,
    _akkaHttp,
    "com.typesafe.akka" %% "akka-http-spray-json" % versionAkkaHttp,
    _akkaHttp2,
    _akkaStreamTestkit % Test,
    _akkaHttpTestkit % Test)

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
    ("com.typesafe.akka" %% "akka-persistence-cassandra" % versionAkkaPersistenceCassandra)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _consulClient = "com.orbitz.consul" % "consul-client" % "1.5.1"

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
    "org.apache.kafka" % "kafka-clients" % versionKafka,
    ("com.typesafe.akka" %% "akka-stream-kafka" % versionAlpakkaKafka)
      .exclude("com.typesafe.akka", "akka-stream")
      .exclude("org.apache.kafka", "kafka-clients")
      .cross(CrossVersion.binary))

  val _akkaDiscoveryConsul = "com.lightbend.akka.discovery" %% "akka-discovery-consul" % versionAkkaManagement

  val _config = "com.typesafe" % "config" % versionConfig

  val _pureconfig = "com.github.pureconfig" %% "pureconfig" % versionPureconfig

  val _chimney = "io.scalaland" %% "chimney" % versionChimney

  val _javaxInject = "javax.inject" % "javax.inject" % "1"

  val _hanlp = "com.hankcs" % "hanlp" % versionHanlp
  val _uuidGenerator = ("com.fasterxml.uuid" % "java-uuid-generator" % versionUuidGenerator).exclude("log4j", "log4j")

  val _guices = Seq(
    "com.google.inject" % "guice" % versionGuice,
    "com.google.inject.extensions" % "guice-assistedinject" % versionGuice)

  val _scalapbJson4s = ("com.thesamet.scalapb" %% "scalapb-json4s" % versionScalapbJson4s)
    .exclude("com.thesamet.scalapb", "scalapb-runtime")

  val _scalapbRuntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion

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
  val _slf4jApi = "org.slf4j" % "slf4j-api" % versionSlf4j
  val _slf4js = _slf4jApi +: Seq("jul-to-slf4j", "jcl-over-slf4j").map("org.slf4j" % _ % versionSlf4j)
  val _logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % versionLogstashLogback
  val _bcprovJdk15on = "org.bouncycastle" % "bcprov-jdk15on" % versionBcprovJdk15on
  val _quartz = ("org.quartz-scheduler" % "quartz" % versionQuartz).exclude("com.zaxxer", "HikariCP-java7")
  val _mybatisPlus = "com.baomidou" % "mybatis-plus" % versionMybatisPlus
  val _lombok = "org.projectlombok" % "lombok" % versionLombok
  val _postgresql = "org.postgresql" % "postgresql" % versionPostgres
  val _mysql = "mysql" % "mysql-connector-java" % versionMySQL
  val _h2 = "com.h2database" % "h2" % versionH2
  val _hikariCP = ("com.zaxxer" % "HikariCP" % versionHikariCP).exclude("org.slf4j", "slf4j-api")
  val _jsch = "com.jcraft" % "jsch" % versionJsch
  val _jakartaMail = "com.sun.mail" % "jakarta.mail" % versionJakartaMail
  val _bson = "org.mongodb" % "bson" % versionBson
  val _mssql = "com.microsoft.sqlserver" % "mssql-jdbc" % versionMssqlJdbc
  val _commonsVfs = "org.apache.commons" % "commons-vfs2" % versionCommonsVfs2
  val _alpnAgent = "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % versionAlpnAgent
  val _joseJwt = "com.nimbusds" % "nimbus-jose-jwt" % versionJoseJwt
  val _pulsar4s = ("com.sksamuel.pulsar4s" %% "pulsar4s-akka-streams" % versionPulsar4sAkkaStreams)
    .exclude("org.apache.pulsar", "pulsar-client")
  val _pulsarClient = "org.apache.pulsar" % "pulsar-client" % versionPulsar
}
