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

scalaVersion in ThisBuild := Dependencies.versionScala

scalafmtOnCompile in ThisBuild := true

lazy val root = Project(id = "akka-fusion", base = file("."))
  .aggregate(
    fusionJob,
    fusionHttpApiGateway,
    fusionHttp,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionTest,
    fusionCore,
    fusionCommon
  )
  .settings(Publishing.noPublish: _*)
  .settings(Environment.settings: _*)

lazy val fusionDocs = _project("fusion-docs")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    fusionJob,
    fusionHttpApiGateway,
    fusionHttp,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionSlick,
    fusionMybatis,
    fusionJdbc,
    fusionTest,
    fusionCore,
    fusionCommon
  )
  .settings(Publishing.noPublish: _*)
  .settings(
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("indigo", "red")
        .withRepository(uri("https://github.com/ihongka/akka-fusion"))
        .withSocial(
          uri("http://ihongka.github.io/akka-fusion/"),
          uri("https://github.com/ihongka"),
          uri("https://weibo.com/yangbajing")
        )
    }
  )

lazy val fusionHttpApiGateway = _project("fusion-http-api-gateway")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      ) ++ _jacksons
  )

lazy val fusionJob = _project("fusion-job")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _quartz
    )
  )

lazy val fusionHttp = _project("fusion-http")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _akkaManagement
    ) ++ _akkaHttps
  )

lazy val fusionOauth = _project("fusion-oauth")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(
    mainClass in Compile := Some("fusion.oauth.fusion.OauthMain"),
    libraryDependencies ++= Seq(
      )
  )

lazy val fusionKafka = _project("fusion-kafka")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _akkaStreamKafka
    )
  )

lazy val fusionMongodb = _project("fusion-mongodb")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionHttp, fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _alpakkaMongodb
    )
  )

lazy val fusionSlick = _project("fusion-slick")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      ) ++ _slicks
  )

lazy val fusionMybatis = _project("fusion-mybatis")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionJdbc, fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _mybatis,
      _mysql % Test
    )
  )

lazy val fusionJdbc = _project("fusion-jdbc")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionTest % "test->test", fusionCore)
  .settings(
    libraryDependencies ++= Seq(
      _hikariCP,
      _postgresql % Test,
      _mysql % Test
    )
  )

lazy val fusionTest = _project("fusion-test")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCore, fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      _chillAkka,
      _akkaTestkit,
      _akkaStreamTestkit,
      _akkaHttpTestkit,
      _scalatest
    )
  )

lazy val fusionCore = _project("fusion-core")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCommon)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      _guice,
      _chillAkka
    )
  )

lazy val fusionCommon = _project("fusion-common")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      _config,
      _bcprovJdk15on,
      _scalaJava8Compat,
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      _scalatest % Test
    ) ++ _akkas ++ _logs ++ _jacksons
  )

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .settings(basicSettings: _*)
    .settings(Publishing.publishing: _*)
    .settings(
      paradoxProperties ++= Map(
        "github.base_url" -> s"https://github.com/ihongka/akka-fusion/tree/${version.value}",
        "version" -> version.value,
        "scala.version" -> scalaVersion.value,
        "scala.binary_version" -> scalaBinaryVersion.value,
        "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
        "akka.version" -> versionAkka
      )
    )
