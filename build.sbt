import Commons._
import Dependencies._
import Environment._

buildEnv in ThisBuild := {
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

scalaVersion in ThisBuild := versionScala212

crossScalaVersions in ThisBuild := Seq(versionScala212, versionScala213)

scalafmtOnCompile in ThisBuild := true

sonarUseExternalConfig in ThisBuild := true

lazy val root = Project(id = "akka-fusion", base = file("."))
  .aggregate(
    fusionInjects,
    fusionJob,
    fusionLog,
    fusionDiscoveryServer,
    fusionDiscoveryClient,
    fusionHttpGateway,
    fusionActuator,
    fusionHttp,
    fusionHttpClient,
    fusionOauth,
    fusionNeo4j,
    fusionKafka,
    fusionMongodb,
    fusionCassandra,
    fusionElasticsearch,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionMail,
    fusionJsonCirce,
    fusionJsonJackson,
    fusionSecurity,
    fusionTest,
    fusionCore,
    fusionCommon,
    helloscalaCommon)
  .settings(Publishing.noPublish: _*)
  .settings(Environment.settings: _*)
  .settings(aggregate in sonarScan := false)
//.settings(
//  addCommandAlias("fix", "all compile:scalafix test:scalafix"),
//  addCommandAlias("fixCheck", "; compile:scalafix --check ; test:scalafix --check"))

lazy val fusionDocs = _project("fusion-docs")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    fusionInjects,
    fusionJob,
    fusionLog,
    fusionJsonCirce,
    fusionDiscoveryServer,
    fusionDiscoveryClient,
    fusionHttpGateway,
    fusionActuator,
    fusionHttp,
    fusionHttpClient,
    fusionOauth,
    fusionNeo4j,
    fusionKafka,
    fusionMongodb,
    fusionCassandra,
    fusionElasticsearch,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionMail,
    fusionSecurity,
    fusionTest,
    fusionCore,
    fusionCommon,
    helloscalaCommon)
  .settings(Publishing.noPublish: _*)
  .settings(
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("indigo", "red")
        .withRepository(uri("https://github.com/ihongka/akka-fusion"))
        .withSocial(
          uri("http://ihongka.github.io/akka-fusion/"),
          uri("https://github.com/ihongka"),
          uri("https://weibo.com/yangbajing"))
    },
    paradoxProperties ++= Map(
        "github.base_url" -> s"https://github.com/ihongka/akka-fusion/tree/${version.value}",
        "version" -> version.value,
        "scala.version" -> scalaVersion.value,
        "scala.binary_version" -> scalaBinaryVersion.value,
        "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
        "akka.version" -> versionAkka))

lazy val fusionInjects = _project("fusion-injects")
  .dependsOn(fusionHttp, fusionDiscoveryClient, fusionTest % "test->test")
  .settings(libraryDependencies ++= Seq(_guice))

lazy val fusionHttpGateway = _project("fusion-http-gateway")
  .dependsOn(fusionHttp, fusionDiscoveryClient, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq())

lazy val fusionDiscoveryServer = _project("fusion-discovery-server")
  .dependsOn(fusionDiscoveryClient, fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq())
  .settings(Publishing.noPublish)

lazy val fusionDiscoveryClient = _project("fusion-discovery-client")
  .dependsOn(fusionHttpClient, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaDiscovery, _nacosClient) ++ _akkaHttps)

lazy val fusionJob = _project("fusion-job")
  .dependsOn(fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_quartz))

lazy val fusionActuatorCluster = _project("fusion-actuator-cluster")
  .dependsOn(fusionActuator, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagementClusterHttp))

lazy val fusionActuator = _project("fusion-actuator")
  .dependsOn(fusionJsonJackson, fusionDiscoveryClient, fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
        _akkaManagement,
        _kamonStatusPage,
        _kamonAkka,
        _kamonAkkaHttp,
        _kamonSystemMetrics,
        _kamonLogback))

lazy val fusionOauth = _project("fusion-oauth")
  .dependsOn(fusionHttpClient, fusionSecurity, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jwt))

lazy val fusionMongodb = _project("fusion-mongodb")
  .dependsOn(fusionHttpClient, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _alpakkaMongodb)

lazy val fusionHttp = _project("fusion-http")
  .dependsOn(fusionHttpClient, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement))

lazy val fusionHttpClient = _project("fusion-http-client")
  .dependsOn(fusionJsonJackson, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= _akkaHttps)

lazy val fusionJsonCirce = _project("fusion-json-circe")
  .dependsOn(fusionTest % "test->test", helloscalaCommon)
  .settings(
    libraryDependencies ++= Seq(
        _akkaHttp,
        _akkaHttpCirce,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,provided") ++ _circes)

lazy val fusionJsonJackson = _project("fusion-json-jackson")
  .dependsOn(fusionTest % "test->test", helloscalaCommon)
  .settings(
    libraryDependencies ++= Seq(
        _akkaHttp,
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,provided") ++ _jacksons)

lazy val fusionLog = _project("fusion-log")
  .dependsOn(fusionTest % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(_logstashLogbackEncoder))

lazy val fusionCassandra = _project("fusion-cassandra")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= _cassandras)

lazy val fusionElasticsearch = _project("fusion-elasticsearch")
  .dependsOn(fusionJsonJackson, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= _elastic4ses)

lazy val fusionKafka = _project("fusion-kafka")
  .dependsOn(fusionJsonJackson, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _akkaStreamKafkas)

lazy val fusionNeo4j = _project("fusion-neo4j")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_neotypes))

lazy val fusionSlick = _project("fusion-slick")
  .dependsOn(fusionJsonJackson, fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_slickPg % Provided) ++ _slicks)

lazy val fusionMybatis = _project("fusion-mybatis")
  .dependsOn(fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_mybatisPlus, _lombok % Provided, _postgresql % Test, _mysql % Test))

lazy val fusionJdbc = _project("fusion-jdbc")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_hikariCP, _postgresql % Test, _mysql % Test))

lazy val fusionDoc =
  _project("fusion-doc").dependsOn(fusionTest % "test->test", fusionCore).settings(libraryDependencies ++= _pois)

lazy val fusionMail = _project("fusion-mail")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jakartaMail))

lazy val fusionSecurity = _project("fusion-security")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_bcprovJdk15on))

lazy val fusionTest = _project("fusion-test")
  .dependsOn(fusionCore, fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        _akkaTestkit,
        _akkaStreamTestkit,
        _akkaHttpTestkit,
        _scalatest))

lazy val fusionCore = _project("fusion-core")
  .dependsOn(fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(libraryDependencies ++= Seq(
      _bson,
      _requests,
      _akkaHttp,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,provided",
      _akkaTestkit % Test,
      _akkaStreamTestkit % Test,
      _akkaHttpTestkit % Test,
      _scalatest % Test) ++ _alpakkas)

lazy val fusionCommon = _project("fusion-common")
  .dependsOn(helloscalaCommon)
  .settings(Publishing.publishing: _*)
  .settings(libraryDependencies ++= Seq(_scalatest % Test))

lazy val helloscalaCommon = _project("helloscala-common")
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        _jacksonAnnotations,
        _scalaJava8Compat,
        _uuidGenerator,
        "org.scala-lang" % "scala-library" % scalaVersion.value,
        _scalatest % Test) ++ _akkas ++ _logs)

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
//.settings(addCompilerPlugin(scalafixSemanticdb))
