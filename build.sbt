import Commons._
import Dependencies.{ _json4s, _ }
import Environment._

ThisBuild / buildEnv := {
  sys.props
    .get("build.env")
    .orElse(sys.env.get("BUILD_ENV"))
    .flatMap {
      case "prod"  => Some(BuildEnv.Production)
      case "stage" => Some(BuildEnv.Stage)
      case "test"  => Some(BuildEnv.Test)
      case "dev"   => Some(BuildEnv.Developement)
      case _       => None
    }
    .getOrElse(BuildEnv.Developement)
}

ThisBuild / scalaVersion := versionScala212

ThisBuild / crossScalaVersions := Seq(versionScala212, versionScala213)

ThisBuild / scalafmtOnCompile := true

ThisBuild / sonarUseExternalConfig := true

lazy val root = Project(id = "akka-fusion", base = file("."))
  .aggregate(
//    fusionSbtPlugin,
//    codegen,
    fusionBoot,
    fusionInjects,
    fusionMq,
    fusionJob,
    fusionLog,
    fusionDiscoveryClient,
    fusionHttpGateway,
    fusionActuator,
    fusionCluster,
    fusionHttp,
    fusionHttpClient,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionCassandra,
    fusionElasticsearch,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionMail,
    fusionJsonJackson,
    fusionJsonCirce,
    fusionSecurity,
    fusionTestkit,
    fusionCore,
    fusionProtobufV3,
    fusionCommon,
    helloscalaCommon)
  .settings(Publishing.noPublish: _*)
  .settings(Environment.settings: _*)
  .settings(aggregate in sonarScan := false, skip in publish := true)
//.settings(
//  addCommandAlias("fix", "all compile:scalafix test:scalafix"),
//  addCommandAlias("fixCheck", "; compile:scalafix --check ; test:scalafix --check"))

lazy val fusionDocs = _project("fusion-docs")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    fusionInjects,
    fusionMq,
    fusionJob,
    fusionLog,
    fusionDiscoveryClient,
    fusionHttpGateway,
    fusionActuator,
    fusionCluster,
    fusionHttpClient,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionCassandra,
    fusionElasticsearch,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionMail,
    fusionSecurity,
    fusionTestkit,
    fusionCore,
    fusionCommon,
    helloscalaCommon)
  .settings(Publishing.noPublish: _*)
  .settings(
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("indigo", "red")
        .withRepository(uri("https://github.com/akka-fusion/akka-fusion"))
        .withSocial(
          uri("https://akka-fusion.github.io/akka-fusion/"),
          uri("https://github.com/akka-fusion"),
          uri("https://weibo.com/yangbajing"))
    },
    paradoxProperties ++= Map(
        "github.base_url" -> s"https://github.com/akka-fusion/akka-fusion/tree/${version.value}",
        "version" -> version.value,
        "scala.version" -> scalaVersion.value,
        "scala.binary_version" -> scalaBinaryVersion.value,
        "scaladoc.akka.base_url" -> s"https://doc.akka.io/api/$versionAkka",
        "akka.version" -> versionAkka))

lazy val fusionSbtPlugin = _project("fusion-sbt-plugin", "sbt-plugin")
  .enablePlugins(SbtPlugin)
  .dependsOn(codegen)
  .settings(
    sbtPlugin := true,
    scalaVersion := versionScala212,
    scriptedBufferLog := false,
    crossScalaVersions := Seq(versionScala212),
    bintrayRepository := "ivy",
    publishMavenStyle := false)

lazy val codegen = _project("codegen")
  .enablePlugins(BuildInfoPlugin)
  .settings(
    scalaVersion := versionScala212,
    crossScalaVersions := Seq(versionScala212, versionScala213),
    publishMavenStyle := false,
    scriptedBufferLog := false,
    bintrayRepository := "ivy",
    buildInfoPackage := "fusion.sbt.gen")

lazy val fusionInjects = _project("fusion-injects")
  .dependsOn(fusionHttp, fusionDiscoveryClient, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq(_guice))

lazy val fusionHttpGateway = _project("fusion-http-gateway")
  .dependsOn(fusionHttp, fusionDiscoveryClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq())

lazy val fusionMq = _project("fusion-mq")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(
    libraryDependencies ++= Seq( /*_alpakkaMqttStreaming*/ ) ++ _akkaClusters ++ _akkaHttps ++ _cassandras ++ _akkaStreamKafkas)

lazy val fusionDiscoveryClient = _project("fusion-discovery-client")
  .dependsOn(fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaDiscovery, _nacosClient) ++ _akkaHttps)

lazy val fusionActuatorCluster = _project("fusion-actuator-cluster")
  .dependsOn(fusionActuator, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagementClusterHttp))

lazy val fusionActuator = _project("fusion-actuator")
  .dependsOn(fusionJsonJackson, fusionDiscoveryClient, fusionTestkit % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
        _akkaManagement,
        _kamonStatusPage,
        _kamonAkka,
        _kamonAkkaHttp,
        _kamonSystemMetrics,
        _kamonLogback) ++ _akkaHttps)

lazy val fusionJob = _project("fusion-job")
  .dependsOn(fusionJdbc, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_quartz))

lazy val fusionOauth = _project("fusion-oauth")
  .dependsOn(fusionJsonJacksonExt, fusionHttpClient, fusionSecurity, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jwt, _jwtJson4s))

lazy val fusionMongodb = _project("fusion-mongodb")
  .dependsOn(fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= _mongodbs)

lazy val fusionCluster = _project("fusion-cluster")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement, _akkaManagementClusterHttp) ++ _akkaClusters)

lazy val fusionHttp = _project("fusion-http")
  .dependsOn(fusionBoot, fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement))

lazy val fusionBoot = _project("fusion-boot").dependsOn(fusionTestkit % "test->test", fusionCore)

lazy val fusionHttpClient = _project("fusion-http-client")
  .dependsOn(fusionProtobufV3, fusionJsonJackson, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided") ++ _akkaHttps)

lazy val fusionJsonCirce = _project("fusion-json-circe")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided",
      _akkaHttp,
      _circeGeneric,
      _scalapbCirce))

lazy val fusionJsonJacksonExt = _project("fusion-json-jackson-ext")
  .dependsOn(fusionJsonJackson, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided",
      _json4s,
      _akkaSerializationJackson))

lazy val fusionJsonJackson = _project("fusion-json-jackson")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(_akkaHttp, _akkaSerializationJackson))

lazy val fusionLog = _project("fusion-log")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(_logstashLogbackEncoder))

lazy val fusionCassandra = _project("fusion-cassandra")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= _cassandras)

lazy val fusionElasticsearch = _project("fusion-elasticsearch")
  .dependsOn(fusionJsonJacksonExt, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= _elastic4ses)

lazy val fusionKafka = _project("fusion-kafka")
  .dependsOn(fusionJsonJackson, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _akkaStreamKafkas)

lazy val fusionSlick = _project("fusion-slick")
  .dependsOn(fusionJsonJackson, fusionJdbc, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_slickPg % Provided) ++ _slicks)

lazy val fusionMybatis = _project("fusion-mybatis")
  .dependsOn(fusionJdbc, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_mybatisPlus, _lombok % Provided, _postgresql % Test, _mysql % Test))

lazy val fusionJdbc = _project("fusion-jdbc")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(_hikariCP, _postgresql % Test, _mysql % Test))

lazy val fusionDoc =
  _project("fusion-doc").dependsOn(fusionTestkit % "test->test", fusionCore).settings(libraryDependencies ++= _pois)

lazy val fusionMail = _project("fusion-mail")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jakartaMail))

lazy val fusionSecurity = _project("fusion-security")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_bcprovJdk15on))

lazy val fusionTestkit = _project("fusion-testkit")
  .dependsOn(fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        _akkaTypedTestkit,
        _akkaStreamTestkit,
        _scalatest))

lazy val fusionCore = _project("fusion-core")
  .dependsOn(fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        _akkaHttp % Provided,
        _akkaTypedTestkit % Test,
        _akkaStreamTestkit % Test,
        _scalatest % Test))

lazy val fusionProtobufV3 = _project("fusion-protobuf-v3")
  .dependsOn(fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,provided",
        _akkaProtobufV3,
        _akkaDiscovery))

lazy val fusionCommon = _project("fusion-common")
  .dependsOn(helloscalaCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        _logbackClassic,
        _akkaSerializationJackson % Provided,
        _akkaTypedTestkit % Test,
        _akkaStreamTestkit % Test,
        _scalatest % Test) ++ _akkas)

lazy val helloscalaCommon = _project("helloscala-common")
  .settings(Publishing.publishing: _*)
  .settings(
    organization := "com.helloscala",
    organizationName := "Helloscala",
    organizationHomepage := Some(url("https://github.com/helloscala")),
    homepage := Some(url("https://helloscala.github.io/helloscala-common")),
    libraryDependencies ++= Seq(
        _jacksonAnnotations % Provided,
        _config,
        _uuidGenerator,
        "org.scala-lang" % "scala-library" % scalaVersion.value,
        _scalaCollectionCompat,
        _scalaJava8Compat,
        _scalaLogging,
        _scalatest % Test))

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
//.settings(addCompilerPlugin(scalafixSemanticdb))
