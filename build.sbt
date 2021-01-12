import Commons._
import Dependencies._
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

ThisBuild / scalaVersion := versionScala213

ThisBuild / crossScalaVersions := Seq(versionScala212, versionScala213)

ThisBuild / scalafmtOnCompile := true

ThisBuild / sonarUseExternalConfig := true

lazy val root = Project(id = "akka-fusion", base = file("."))
  .aggregate(
    fusionInjectGuiceTestkit,
    fusionInjectGuice,
    fusionInject,
    fusionJob,
    fusionLog,
    fusionGrpc,
    fusionHttpGateway,
    fusionCloudNacos,
    fusionCloudConsul,
    fusionCloud,
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
    fusionAuthorizationServer,
    fusionSecurityOauthJose,
    fusionSecurityOauthCore,
    fusionJsonJacksonExt,
    fusionJsonJackson,
    fusionSecurity,
    fusionTestkit,
    fusionProtobufV3,
    fusionCore,
    helloscalaCommon
  )
  .settings(Publishing.noPublish: _*)
  .settings(Environment.settings: _*)
  .settings(aggregate in sonarScan := false, skip in publish := true)
//.settings(
//  addCommandAlias("fix", "all compile:scalafix test:scalafix"),
//  addCommandAlias("fixCheck", "; compile:scalafix --check ; test:scalafix --check"))

lazy val fusionDocs = _project("fusion-docs")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    fusionInjectGuice,
    fusionJob,
    fusionLog,
    fusionHttpGateway,
    fusionActuator,
    fusionCluster,
    fusionCloudNacos,
    fusionCloudConsul,
    fusionCloud,
    fusionHttpClient,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionCassandra,
    fusionElasticsearch,
    fusionSlick,
    fusionMybatis,
    fusionJsonJacksonExt,
    fusionJdbc,
    fusionMail,
    fusionSecurity,
    fusionTestkit,
    fusionCore,
    helloscalaCommon
  )
  .settings(Publishing.noPublish: _*)
  .settings(
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("indigo", "red")
        .withRepository(uri("https://github.com/akka-fusion/akka-fusion"))
        .withSocial(
          uri("https://akka-fusion.github.io/akka-fusion/"),
          uri("https://github.com/akka-fusion"),
          uri("https://weibo.com/yangbajing")
        )
    },
    paradoxProperties ++= Map(
      "github.base_url" -> s"https://github.com/akka-fusion/akka-fusion/tree/${version.value}",
      "version" -> version.value,
      "scala.version" -> scalaVersion.value,
      "scala.binary_version" -> scalaBinaryVersion.value,
      "scaladoc.akka.base_url" -> s"https://doc.akka.io/api/$versionAkka",
      "akka.version" -> versionAkka
    )
  )

lazy val fusionSbtPlugin = _project("fusion-sbt-plugin", "sbt-plugin")
  .enablePlugins(SbtPlugin)
  .dependsOn(codegen)
  .settings(
    sbtPlugin := true,
    scalaVersion := versionScala212,
//    bintrayRepository := "ivy",
    scriptedBufferLog := false,
    publishMavenStyle := false
  )

lazy val codegen = _project("codegen")
  .enablePlugins(BuildInfoPlugin)
  .settings(
    scalaVersion := versionScala212,
//    bintrayRepository := "ivy",
    scriptedBufferLog := false,
    publishMavenStyle := false,
    buildInfoPackage := "fusion.sbt.gen"
  )

lazy val fusionInjectGuiceTestkit = _project("fusion-inject-guice-testkit").dependsOn(fusionInjectGuice, fusionTestkit)

lazy val fusionInjectGuice = _project("fusion-inject-guice")
  .dependsOn(fusionInject, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= _guices)

lazy val fusionInject = _project("fusion-inject")
  .dependsOn(fusionHttp, fusionCloud, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq(_javaxInject))

lazy val fusionHttpGateway = _project("fusion-http-gateway")
  .dependsOn(fusionHttp, fusionCloud, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq())

lazy val fusionCloudConsul = _project("fusion-cloud-consul")
  .dependsOn(fusionCloud, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaDiscoveryConsul) ++ _akkaHttps)

lazy val fusionCloudNacos = _project("fusion-cloud-nacos")
  .dependsOn(fusionCloud, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_nacosClient) ++ _akkaHttps)

lazy val fusionCloud = _project("fusion-cloud")
  .dependsOn(fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(_akkaManagement, _chimney, _akkaDiscovery) ++ _akkaHttps ++ _akkaClusters
      .map(_ % Provided)
  )

lazy val fusionActuatorCluster = _project("fusion-actuator-cluster")
  .dependsOn(fusionActuator, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagementClusterHttp))

lazy val fusionActuator = _project("fusion-actuator")
  .dependsOn(fusionHttp, fusionTestkit % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _akkaManagement,
      _kamonStatusPage,
      _kamonAkka,
      _kamonAkkaHttp,
      _kamonSystemMetrics,
      _kamonLogback
    ) ++ _akkaHttps
  )

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

lazy val fusionGrpc = _project("fusion-grpc")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_scalapbJson4s))

lazy val fusionAuthorizationServer = _project("fusion-authorization-server")
  .dependsOn(fusionSecurityOauthCore, fusionHttp, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq())

lazy val fusionSecurityOauthJose = _project("fusion-security-oauth-jose")
  .dependsOn(fusionSecurityOauthCore, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq(_joseJwt))

lazy val fusionSecurityOauthCore = _project("fusion-security-oauth-core")
  .dependsOn(fusionSecurity, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq())

lazy val fusionHttp = _project("fusion-http")
  .dependsOn(fusionCloudConsul % "test->test", fusionCloud, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement, _logbackClassic % Test))

lazy val fusionHttpClient = _project("fusion-http-client")
  .dependsOn(fusionProtobufV3, fusionJsonJackson, fusionTestkit % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided"
    ) ++ _akkaHttps
  )

lazy val fusionJsonJacksonExt = _project("fusion-json-jackson-ext")
  .dependsOn(fusionJsonJackson, fusionTestkit % "test->test")
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided",
      _json4s
    )
  )

lazy val fusionJsonJackson = _project("fusion-json-jackson")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaHttp, _akkaSerializationJackson))

lazy val fusionLog = _project("fusion-log")
  .dependsOn(fusionTestkit % "test->test", helloscalaCommon)
  .settings(libraryDependencies ++= Seq(_logbackClassic, _logstashLogbackEncoder) ++ _slf4js)

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
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_hikariCP, _postgresql % Test, _mysql % Test))

lazy val fusionDoc =
  _project("fusion-doc").dependsOn(fusionTestkit % "test->test", fusionCore).settings(libraryDependencies ++= _pois)

lazy val fusionMail = _project("fusion-mail")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jakartaMail))

lazy val fusionSecurity = _project("fusion-security")
  .dependsOn(fusionJsonJackson, fusionCore, fusionTestkit % "test->test")
  .settings(libraryDependencies ++= Seq(_bcprovJdk15on))

lazy val fusionTestkit = _project("fusion-testkit")
  .dependsOn(fusionCore)
  .settings(Publishing.publishing: _*)
  .settings(libraryDependencies ++= Seq(_akkaTypedTestkit, _akkaStreamTestkit, _scalatest))

lazy val fusionProtobufV3 = _project("fusion-protobuf-v3")
  .dependsOn(fusionCore)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,provided",
      _akkaProtobufV3,
      _akkaDiscovery
    )
  )

lazy val fusionCore = _project("fusion-core")
  .dependsOn(helloscalaCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      _akkaHttp % Provided,
      _logbackClassic % Provided,
      _pureconfig,
      _akkaTypedTestkit % Test,
      _akkaStreamTestkit % Test,
      _scalatest % Test
    ) ++ _akkas ++ _slf4js
  )

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
      _slf4jApi,
      _scalaLogging,
      _uuidGenerator,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      _scalaCollectionCompat,
      _scalaJava8Compat,
      _scalatest % Test
    )
  )

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
//.settings(addCompilerPlugin(scalafixSemanticdb))
