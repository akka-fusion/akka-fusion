import Dependencies.{ versionScala212, versionScala213 }
import bintray.BintrayKeys._
import com.typesafe.sbt.SbtNativePackager.autoImport.maintainer
import de.heikoseeberger.sbtheader.HeaderPlugin.autoImport.{ HeaderLicense, headerLicense }
import sbt.Keys._
import sbt._

object Commons {
  import Environment.{ BuildEnv, buildEnv }

  def basicSettings =
    Seq(
      organization := "com.helloscala.fusion",
      organizationName := "Akka Fusion",
      organizationHomepage := Some(url("https://github.com/helloscala")),
      homepage := Some(url("https://akka-fusion.github.io/akka-fusion")),
      startYear := Some(2019),
      licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
      headerLicense := Some(HeaderLicense.ALv2("2019", "helloscala.com")),
      crossScalaVersions := Seq(versionScala212, versionScala213),
      scalacOptions ++= {
        var list = Seq(
          "-encoding",
          "UTF-8", // yes, this is 2 args
          "-feature",
          "-deprecation",
          "-unchecked",
          //"-Yno-adapted-args", //akka-http heavily depends on adapted args and => Unit implicits break otherwise
          //"-Ypartial-unification",
          "-Ywarn-dead-code",
          //"-Yrangepos", // required by SemanticDB compiler plugin
          //"-Ywarn-unused-import", // required by `RemoveUnused` rule
          "-Xlint")
        //if (scalaVersion.value.startsWith("2.12")) {
        //  list ++= Seq("-opt:l:inline", "-opt-inline-from")
        //}
        if (buildEnv.value != BuildEnv.Developement) {
          list ++= Seq("-Xelide-below", "2001")
        }
        list
      },
      javacOptions in Compile ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
      javaOptions in run ++= Seq("-Xms128m", "-Xmx1024m", "-Djava.library.path=./target/native"),
      shellPrompt := { s =>
        Project.extract(s).currentProject.id + " > "
      },
      resolvers += Resolver.bintrayRepo("akka", "snapshots"),
      fork in run := true,
      fork in Test := true,
      parallelExecution in Test := false,
      libraryDependencies ++= Seq(Dependencies._scalatest % Test)) ++ Environment.settings // ++ Formatting.settings
}

object Publishing {
  lazy val publishing = Seq(
    bintrayOrganization := Some("helloscala"),
    bintrayRepository := "maven",
    maintainer := "yangbajing <yang.xunjing@qq.com>",
    developers := List(
      Developer(
        id = "yangbajing",
        name = "Yang Jing",
        email = "yang.xunjing@qq.com",
        url = url("https://github.com/yangbajing"))),
    scmInfo := Some(
      ScmInfo(url("https://github.com/akka-fusion/akka-fusion"), "scm:git:git@github.com:akka-fusion/akka-fusion.git")))

  lazy val noPublish =
    Seq(publish := ((): Unit), publishLocal := ((): Unit), publishTo := None)
}

object Environment {
  object BuildEnv extends Enumeration {
    val Production, Stage, Test, Developement = Value
  }

  val buildEnv = settingKey[BuildEnv.Value]("The current build environment")

  val settings = Seq(onLoadMessage := {
    // old message as well
    val defaultMessage = onLoadMessage.value
    val env = buildEnv.value
    s"""|$defaultMessage
          |Working in build environment: $env""".stripMargin
  })
}
