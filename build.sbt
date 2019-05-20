import Commons._
import Dependencies._
import Environment._

buildEnv in Global := {
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

scalaVersion in Global := Dependencies.versionScala

scalafmtOnCompile in Global := true

lazy val root = Project(id = "akka-fusion", base = file("."))
  .aggregate(
    fusionInjects,
    fusionJob,
    fusionDiscoveryServer,
    fusionDiscoveryClient,
    fusionHttpApiGateway,
    fusionActuator,
    fusionHttp,
    fusionOauth,
    fusionNeo4j,
    fusionKafka,
    fusionMongodb,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionMail,
    fusionTest,
    fusionCore,
    fusionCommon,
    helloscalaCommon)
  .settings(Publishing.noPublish: _*)
  .settings(Environment.settings: _*)
  .settings(
    addCommandAlias("fix", "all compile:scalafix test:scalafix"),
    addCommandAlias("fix", "all compile:scalafix test:scalafix"))

lazy val fusionDocs = _project("fusion-docs")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    fusionInjects,
    fusionJob,
    fusionDiscoveryServer,
    fusionDiscoveryClient,
    fusionHttpApiGateway,
    fusionActuator,
    fusionHttp,
    fusionOauth,
    fusionNeo4j,
    fusionKafka,
    fusionMongodb,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionMail,
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
      "github.base_url"        -> s"https://github.com/ihongka/akka-fusion/tree/${version.value}",
      "version"                -> version.value,
      "scala.version"          -> scalaVersion.value,
      "scala.binary_version"   -> scalaBinaryVersion.value,
      "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
      "akka.version"           -> versionAkka))

lazy val fusionInjects = _project("fusion-injects")
  .dependsOn(fusionHttp, fusionDiscoveryClient, fusionTest % "test->test")
  .settings(libraryDependencies ++= Seq(_guice))

lazy val fusionHttpApiGateway = _project("fusion-http-api-gateway")
  .dependsOn(fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq())
  .settings(Publishing.noPublish)

lazy val fusionDiscoveryServer = _project("fusion-discovery-server")
  .dependsOn(fusionDiscoveryClient, fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq())
  .settings(Publishing.noPublish)

lazy val fusionDiscoveryClient = _project("fusion-discovery-client")
  .dependsOn(fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_nacosClient))

lazy val fusionJob =
  _project("fusion-job").dependsOn(fusionTest % "test->test", fusionCore).settings(libraryDependencies ++= Seq(_quartz))

lazy val fusionActuator = _project("fusion-actuator")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement) ++ _akkaHttps)

lazy val fusionHttp = _project("fusion-http")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_akkaManagement) ++ _akkaHttps)

lazy val fusionOauth = _project("fusion-oauth")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(mainClass in Compile := Some("fusion.oauth.fusion.OauthMain"), libraryDependencies ++= Seq(_jwt))

lazy val fusionKafka = _project("fusion-kafka")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _akkaStreamKafkas)

lazy val fusionNeo4j = _project("fusion-neo4j")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_neotypes))

lazy val fusionMongodb = _project("fusion-mongodb")
  .dependsOn(fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _alpakkaMongodb)

lazy val fusionSlick = _project("fusion-slick")
  .dependsOn(fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq() ++ _slicks)

lazy val fusionMybatis = _project("fusion-mybatis")
  .dependsOn(fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_mybatis, _mysql % Test))

lazy val fusionJdbc = _project("fusion-jdbc")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_hikariCP, _postgresql % Test, _mysql % Test))

lazy val fusionMail = _project("fusion-mail")
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(libraryDependencies ++= Seq(_jakartaMail))

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

lazy val fusionCore =
  _project("fusion-core")
    .dependsOn(fusionCommon)
    .settings(Publishing.publishing: _*)
    .settings(
      libraryDependencies ++= Seq(
        "org.scala-lang" % "scala-reflect" % scalaVersion.value,
        _scalameta,
        _osLib,
        _chillAkka) ++ _alpakkas)

lazy val fusionCommon = _project("fusion-common")
  .dependsOn(helloscalaCommon)
  .settings(Publishing.publishing: _*)
  .settings(libraryDependencies ++= Seq(_scalatest % Test) ++ _akkas)

lazy val helloscalaCommon = _project("helloscala-common")
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      _config,
      _requests,
      _bcprovJdk15on,
      _scalaJava8Compat,
      _akkaStream,
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      _scalatest       % Test) ++ _logs ++ _jacksons ++ _pois)

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
    .settings(
//      addCompilerPlugin(scalafixSemanticdb)
    )
