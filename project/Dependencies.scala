import sbt._

object Dependencies {
  val versionScala212 = "2.12.9"
  val versionScala213 = "2.13.0"
  val versionScalaXml = "1.2.0"
  val versionJava8Compat = "0.9.0"
  val versionScalameta = "4.2.1"
  val versionScalatest = "3.0.8"
  val versionAkka = "2.5.25"
  val versionAkkaManagement = "1.0.2"
  val versionAkkaHttp = "10.1.9"
  val versionAkkaHttpCors = "0.4.1"
  val versionAkkaHttpCirce = "1.27.0"
  val versionAlpakka = "1.1.1"
  val versionAlpakkaKafka = "1.0.5"
  val versionCassandra = "4.1.0"
  val versionElastic4s = "6.7.1"
  val versionCirce = "0.11.1"
  val versionConfig = "1.3.4"
  val versionGuice = "4.2.2"
  val versionNeotypes = "0.11.0"
  val versionKamon = "2.0.0"
  val versionKanela = "1.0.0"
  val versionJackson = "2.9.9"
  val versionUuidGenerator = "3.2.0"
  val versionHanlp = "portable-1.7.4"
  val versionSlick = "3.3.2"
  val versionSlickPg = "0.18.0"
  val versionPoi = "4.1.0"
  val versionQuartz = "2.3.1"
  val versionBcprovJdk15on = "1.62"
  val versionNacos = "1.1.3"
  val versionJsch = "0.1.55"
  val versionJakartaMail = "1.6.3"
  val versionHikariCP = "3.3.1"
  val versionMybatisPlus = "3.1.2"
  val versionLombok = "1.18.8"
  val versionMySQL = "8.0.16"
  val versionPostgres = "42.2.6"
  val versionRequests = "0.2.0"
  val versionFastparse = "2.1.3"
  val versionOsLib = "0.3.0"
  val versionMongoScalaBson = "2.6.0"
  val versionBson = "3.10.2"
  val versionChillAkka = "0.9.3"
  val versionKafka = "2.1.1"
  val versionAlpnAgent = "2.0.9"
  val versionLogback = "1.2.3"
  val versionScalaLogging = "3.9.2"
  val versionLogstashLogback = "6.1"

  val _scalameta = "org.scalameta" %% "scalameta" % versionScalameta
  val _scalaXml = ("org.scala-lang.modules" %% "scala-xml" % versionScalaXml).exclude("org.scala-lang", "scala-library")

  val _scalaJava8Compat =
    ("org.scala-lang.modules" %% "scala-java8-compat" % versionJava8Compat).exclude("org.scala-lang", "scala-library")
  val _scalatest = "org.scalatest" %% "scalatest" % versionScalatest
  val _akkaRemote = "com.typesafe.akka" %% "akka-remote" % versionAkka
  val _akkaStream = "com.typesafe.akka" %% "akka-stream" % versionAkka
  val _akkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % versionAkka
  val _akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % versionAkka
  val _akkaStreamTestkit = "com.typesafe.akka" %% "akka-stream-testkit" % versionAkka

  val _akkas =
    Seq("com.typesafe.akka" %% "akka-slf4j" % versionAkka, _akkaStream, _akkaTestkit % Test, _akkaStreamTestkit % Test)
      .map(_.exclude("org.scala-lang.modules", "scala-java8-compat").cross(CrossVersion.binary))
  val _akkaPersistence = "com.typesafe.akka" %% "akka-persistence-query" % versionAkka
  val _akkaMultiNodeTestkit = "com.typesafe.akka" %% "akka-multi-node-testkit" % versionAkka

  val _akkaClusters = Seq(
    "com.typesafe.akka" %% "akka-cluster" % versionAkka,
    //"com.typesafe.akka" %% "akka-cluster-typed"          % versionAkka,
    "com.typesafe.akka" %% "akka-cluster-tools" % versionAkka,
    "com.typesafe.akka" %% "akka-cluster-metrics" % versionAkka,
    "com.typesafe.akka" %% "akka-cluster-sharding" % versionAkka,
    //"com.typesafe.akka" %% "akka-cluster-sharding-typed" % versionAkka,
    _akkaMultiNodeTestkit % Test)

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

  val _akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % versionAkkaHttp
  val _akkaHttp = "com.typesafe.akka" %% "akka-http" % versionAkkaHttp
  val _akkaHttp2Support = "com.typesafe.akka" %% "akka-http2-support" % versionAkkaHttp
  val _akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % versionAkkaHttp

  val _akkaHttpCors =
    ("ch.megard" %% "akka-http-cors" % versionAkkaHttpCors).excludeAll(ExclusionRule("com.typesafe.akka"))

  val _akkaHttps = Seq(_akkaHttp, _akkaHttp2Support, _akkaHttpCors, _akkaHttpTestkit % Test).map(
    _.exclude("com.typesafe.akka", "akka-stream")
      .cross(CrossVersion.binary)
      .exclude("com.typesafe.akka", "akka-stream-testkit")
      .cross(CrossVersion.binary))

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

  val _alpakkaMongodb = Seq(
    ("com.lightbend.akka" %% "akka-stream-alpakka-mongodb" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary),
    "org.mongodb.scala" %% "mongo-scala-bson" % versionMongoScalaBson)

  val _cassandras = Seq("com.datastax.oss" % "java-driver-core" % versionCassandra)

  val _akkaHttpCirce = ("de.heikoseeberger" %% "akka-http-circe" % versionAkkaHttpCirce)
    .excludeAll(ExclusionRule("io.circe"), ExclusionRule("com.typesafe.akka"))

  val _circes =
    Seq("io.circe" %% "circe-core", "io.circe" %% "circe-generic", "io.circe" %% "circe-parser").map(_ % versionCirce)

  val _elastic4ses = Seq(
      "com.sksamuel.elastic4s" %% "elastic4s-core" % versionElastic4s,
      "com.sksamuel.elastic4s" %% "elastic4s-http-streams" % versionElastic4s,
      ("com.sksamuel.elastic4s" %% "elastic4s-circe" % versionElastic4s).excludeAll(ExclusionRule("io.circe")),
      "com.sksamuel.elastic4s" %% "elastic4s-testkit" % versionElastic4s % Test) ++ _circes

  val _alpakkaElasticsearch =
    ("com.lightbend.akka" %% "akka-stream-alpakka-elasticsearch" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaHbase =
    ("com.lightbend.akka" %% "akka-stream-alpakka-hbase" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakksHdfs =
    ("com.lightbend.akka" %% "akka-stream-alpakka-hdfs" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkaText =
    ("com.lightbend.akka" %% "akka-stream-alpakka-text" % versionAlpakka)
      .excludeAll(ExclusionRule("com.typesafe.akka"))
      .cross(CrossVersion.binary)

  val _alpakkas = Seq(
    _alpakkaText,
    _alpakkaSimpleCodecs,
    _alpakkaXml,
    _alpakkaCsv,
    //_alpakkaJsonStreaming,
    _alpakkaFile)

  val _akkaStreamKafkas = Seq(
    ("com.typesafe.akka" %% "akka-stream-kafka" % versionAlpakkaKafka)
      .exclude("com.typesafe.akka", "akka-slf4j")
      .cross(CrossVersion.binary))

  val _chillAkka = ("com.twitter" %% "chill-akka" % versionChillAkka)
    .exclude("com.typesafe", "config")
    .exclude("org.scala-lang", "scala-library")
  val _neotypes = "com.dimafeng" %% "neotypes" % versionNeotypes
  val _neotypesAkkaStream = "com.dimafeng" %% "neotypes-akka-stream" % versionNeotypes
  val _pureconfig = "com.github.pureconfig" %% "pureconfig" % "0.11.1"
  val _hanlp = "com.hankcs" % "hanlp" % versionHanlp
  val _uuidGenerator = ("com.fasterxml.uuid" % "java-uuid-generator" % versionUuidGenerator).exclude("log4j", "log4j")
  val _guice = "com.google.inject" % "guice" % versionGuice

  val _jacksonAnnotations = "com.fasterxml.jackson.core" % "jackson-annotations" % versionJackson
  val _jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % versionJackson

  val _jacksons = Seq(
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jdk8" % versionJackson,
    "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % versionJackson,
    "com.fasterxml.jackson.module" % "jackson-module-parameter-names" % versionJackson,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % versionJackson)
  val _jacksonDataformatProtobuf = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-protobuf" % versionJackson

  val _kanelaAgent = "io.kamon" % "kanela-agent" % versionKanela % Provided

  val _kamonStatusPage = "io.kamon" %% "kamon-status-page" % versionKamon

  val _kamonAkka = ("io.kamon" %% "kamon-akka" % versionKamon)
    .excludeAll("com.typesafe.akka")
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")

  val _kamonAkkaHttp = ("io.kamon" %% "kamon-akka-http" % versionKamon)
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

  val _kamonSystemMetrics = ("io.kamon" %% "kamon-system-metrics" % versionKamon)
    .exclude("io.kamon", "kamon-core")
    .cross(CrossVersion.binary)
    .exclude("org.scala-lang", "scala-library")
  val _kamonPrometheus = "io.kamon" %% "kamon-prometheus" % versionKamon
  val _kamonZipkin = "io.kamon" %% "kamon-zipkin" % versionKamon
  val _kamonLogback = "io.kamon" %% "kamon-logback" % versionKamon
  val _scopt = "com.github.scopt" %% "scopt" % "3.7.1"
  val _osLib = "com.lihaoyi" %% "os-lib" % versionOsLib
  val _requests = "com.lihaoyi" %% "requests" % versionRequests
  val _fastparse = "com.lihaoyi" %% "fastparse" % versionFastparse
  val _shapeless = "com.chuusai" %% "shapeless" % "2.3.3"
  val _jwt = "com.pauldijou" %% "jwt-core" % "2.1.0"

  val _slicks =
    Seq("com.typesafe.slick" %% "slick" % versionSlick, "com.typesafe.slick" %% "slick-testkit" % versionSlick % Test)
  val _slickPg = "com.github.tminglei" %% "slick-pg" % versionSlickPg

  val _pois = Seq("org.apache.poi" % "poi-scratchpad" % versionPoi, "org.apache.poi" % "poi-ooxml" % versionPoi)

  val _logs = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % versionScalaLogging,
    "ch.qos.logback" % "logback-classic" % versionLogback)

  val _logstashLogbackEncoder = "net.logstash.logback" % "logstash-logback-encoder" % versionLogstashLogback

  val _bcprovJdk15on = "org.bouncycastle" % "bcprov-jdk15on" % versionBcprovJdk15on
  val _quartz = ("org.quartz-scheduler" % "quartz" % versionQuartz).exclude("com.zaxxer", "HikariCP-java7")
  val _mybatisPlus = "com.baomidou" % "mybatis-plus" % versionMybatisPlus
  val _lombok = "org.projectlombok" % "lombok" % versionLombok
  val _postgresql = "org.postgresql" % "postgresql" % versionPostgres
  val _mysql = "mysql" % "mysql-connector-java" % versionMySQL
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
