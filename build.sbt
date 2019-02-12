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

scalaVersion in ThisBuild := Dependencies.versionScala

scalafmtOnCompile in ThisBuild := true

lazy val root = Project(id = "akka-fusion", base = file("."))
  .aggregate(
    fusionDocs,
    fusionHttpRest,
    fusionHttp,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionSlick,
    fusionJdbc,
    fusionCore,
    fusionCommon
  )
  .settings(Publishing.noPublish: _*)
  .settings(Environment.settings: _*)

lazy val fusionDocs = _project("fusion-docs")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(
    fusionHttpRest,
    fusionHttp,
    fusionOauth,
    fusionKafka,
    fusionMongodb,
    fusionSlick,
    fusionJdbc,
    fusionCore,
    fusionCommon
  )
  .settings(Publishing.noPublish: _*)
  .settings(
    Compile / paradoxMaterialTheme ~= {
      _.withLanguage(java.util.Locale.SIMPLIFIED_CHINESE)
        .withColor("teal", "indigo")
        .withRepository(uri("https://github.com/helloscala/akka-fusion"))
        .withSocial(
          uri("http://fusion.helloscala.com/"),
          uri("https://github.com/yangbajing"),
          uri("https://weibo.com/yangbajing")
        )
    }
  )

lazy val fusionHttpRest = _project("fusion-http-rest")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionHttp, fusionCore % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      ) ++ _jacksons
  )

lazy val fusionHttp = _project("fusion-http")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCore % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      ) ++ _akkaHttps
  )

lazy val fusionOauth = _project("fusion-oauth")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCore % "compile->compile;test->test")
  .settings(
    mainClass in Compile := Some("fusion.oauth.fusion.OauthMain"),
    libraryDependencies ++= Seq(
      )
  )

lazy val fusionKafka = _project("fusion-kafka")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCore % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      _akkaStreamKafka
    )
  )

lazy val fusionMongodb = _project("fusion-mongodb")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCore % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      _alpakkaMongodb
    )
  )

lazy val fusionSlick = _project("fusion-slick")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionJdbc, fusionCore % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      ) ++ _slicks
  )

lazy val fusionJdbc = _project("fusion-jdbc")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCore % "compile->compile;test->test")
  .settings(
    libraryDependencies ++= Seq(
      _hikariCP
    )
  )

lazy val fusionCore = _project("fusion-core")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .dependsOn(fusionCommon % "compile->compile;test->test")
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      _chillAkka
    )
  )

lazy val fusionCommon = _project("fusion-common")
  .enablePlugins(ParadoxMaterialThemePlugin)
  .settings(Publishing.publishing: _*)
  .settings(
    libraryDependencies ++= Seq(
      _config,
      "org.scala-lang" % "scala-library" % scalaVersion.value,
      _scalaJava8Compat,
      _scalatest % Test
    ) ++ _akkas ++ _logs ++ _jacksons
  )

def _project(name: String, _base: String = null) =
  Project(id = name, base = file(if (_base eq null) name else _base))
    .settings(basicSettings: _*)
    .settings(
      paradoxProperties ++= Map(
        "github.base_url" -> s"https://github.com/helloscala/akka-fusion/tree/${version.value}",
        "scala.version" -> scalaVersion.value,
        "scala.binary_version" -> scalaBinaryVersion.value,
        "scaladoc.akka.base_url" -> s"http://doc.akka.io/api/$versionAkka",
        "akka.version" -> versionAkka
      )
    )
