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
    fusionBoot,
    fusionInjects,
    fusionMq,
    fusionJob,
    fusionLog,
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
    fusionJson,
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
          uri("http://akka-fusion.github.io/akka-fusion/"),
          uri("https://github.com/akka-fusion"),
          uri("https://weibo.com/yangbajing"))
    },
    paradoxProperties ++= Map(
        "github.base_url" -> s"https://github.com/akka-fusion/akka-fusion/tree/${version.value}",
        "version" -> version.value,
        "scala.version" -> scalaVersion.value,
        "scala.binary_version" -> scalaBinaryVersion.value,
        "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
        "akka.version" -> versionAkka))

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

//lazy val fusionDiscoveryServer = _project("fusion-discovery-server")
//  .enablePlugins(AkkaGrpcPlugin /*, MultiJvmPlugin*/, JavaAgent)
//  .dependsOn(fusionJsonCirce, fusionDiscoveryClient, fusionHttp, fusionLog, fusionTestkit % "test->test", fusionCore)
//  //  .settings(MultiJvmPlugin.multiJvmSettings: _*)
//  .settings(Publishing.noPublish)
//  .settings(Packaging.assemblySettings: _*)
//  .settings(
//    javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
//    javaAgents += _alpnAgent % "runtime;test",
//    akkaGrpcCodeGeneratorSettings += "server_power_apis",
//    libraryDependencies ++= Seq(
//        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
//        "org.iq80.leveldb" % "leveldb" % "0.7",
//        "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
//        _akkaGrpcRuntime,
//        _akkaSerializationJackson,
//        _akkaPersistenceCassandra,
//        _akkaPersistenceJdbc,
//        _hikariCP,
//        _postgresql,
//        _akkaPersistenceTyped,
//        _akkaMultiNodeTestkit % Test) ++ _akkaClusters)
//  .configs(MultiJvm)

lazy val fusionDiscoveryClient = _project("fusion-discovery-client")
  .dependsOn(fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaDiscovery, _nacosClient) ++ _akkaHttps)

lazy val fusionActuatorCluster = _project("fusion-actuator-cluster")
  .dependsOn(fusionActuator, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagementClusterHttp))

lazy val fusionActuator = _project("fusion-actuator")
  .dependsOn(fusionJson, fusionDiscoveryClient, fusionTestkit % "test->test", fusionCore)
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
  .dependsOn(fusionHttpClient, fusionSecurity, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jwt, _jwtJson4s))

lazy val fusionMongodb = _project("fusion-mongodb")
  .dependsOn(fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= _mongodbs)

lazy val fusionHttp = _project("fusion-http")
  .dependsOn(fusionBoot, fusionHttpClient, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement))

lazy val fusionBoot = _project("fusion-boot").dependsOn(fusionTestkit % "test->test", fusionCore)

lazy val fusionHttpClient = _project("fusion-http-client")
  .dependsOn(fusionProtobufV3, fusionJson, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided") ++ _akkaHttps)

lazy val fusionJsonCirce = _project("fusion-json-circe")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided",
      _akkaHttp,
      _circeGeneric,
      _scalapbCirce))

lazy val fusionJson = _project("fusion-json")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,test,provided",
      _akkaHttp,
      _akkaSerializationJackson,
      _json4s))

lazy val fusionLog = _project("fusion-log")
  .dependsOn(fusionTestkit % "test->test", fusionCommon)
  .settings(libraryDependencies ++= Seq(_logstashLogbackEncoder))

lazy val fusionCassandra = _project("fusion-cassandra")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= _cassandras)

lazy val fusionElasticsearch = _project("fusion-elasticsearch")
  .dependsOn(fusionJson, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= _elastic4ses)

lazy val fusionKafka = _project("fusion-kafka")
  .dependsOn(fusionJson, fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _akkaStreamKafkas)

lazy val fusionNeo4j = _project("fusion-neo4j")
  .dependsOn(fusionTestkit % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_neotypes))

lazy val fusionSlick = _project("fusion-slick")
  .dependsOn(fusionJson, fusionJdbc, fusionTestkit % "test->test", fusionCore)
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
  .enablePlugins(AkkaGrpcPlugin)
  .dependsOn(fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf,provided",
        _akkaProtobufV3,
        _akkaGrpcRuntime,
        _akkaDiscovery))

lazy val fusionCommon = _project("fusion-common")
  .dependsOn(helloscalaCommon)
  //  .enablePlugins(BuildInfoPlugin)
  .settings(Publishing.publishing: _*)
  .settings(
//    buildInfoKeys := Seq[BuildInfoKey](
//        startYear,
//        organization,
//        organizationName,
//        organizationHomepage,
//        scalacOptions,
//        javacOptions,
//        version,
//        scalaVersion,
//        sbtVersion,
//        sbtBinaryVersion,
//        git.gitCurrentTags,
//        git.gitDescribedVersion,
//        git.gitCurrentBranch,
//        git.gitHeadCommit,
//        git.gitHeadCommitDate),
//    buildInfoOptions += BuildInfoOption.BuildTime,
//    buildInfoPackage := "fusion.version",
//    buildInfoObject := "Version",
    libraryDependencies ++= Seq(
        _akkaSerializationJackson % Provided,
        _akkaTypedTestkit % Test,
        _akkaStreamTestkit % Test,
        _scalatest % Test) ++ _akkas)

lazy val helloscalaCommon = _project("helloscala-common")
  .settings(Publishing.publishing: _*)
  .settings(libraryDependencies ++= Seq(
      _jacksonAnnotations % Provided,
      _config,
      _uuidGenerator,
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      _scalaCollectionCompat,
      _scalaJava8Compat,
      _scalatest % Test) ++ _logs)

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .enablePlugins(AutomateHeaderPlugin)
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
//.settings(addCompilerPlugin(scalafixSemanticdb))
