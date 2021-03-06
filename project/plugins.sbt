logLevel := Level.Info

resolvers += Resolver.sbtPluginRepo("releases")
resolvers += Resolver.bintrayIvyRepo("2m", "sbt-plugins")

libraryDependencies += Defaults.sbtPluginExtra(
  "com.eed3si9n" % "sbt-assembly" % "0.15.0",
  (sbtBinaryVersion in pluginCrossBuild).value,
  (scalaBinaryVersion in pluginCrossBuild).value)

addDependencyTreePlugin

addSbtPlugin("com.github.mwz" % "sbt-sonar" % "1.6.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.5.2")
addSbtPlugin("io.github.jonas" % "sbt-paradox-material-theme" % "0.6.0")
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.0.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.5")
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "1.1.1")
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.4.4")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.2")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.24")
//addSbtPlugin("org.foundweekends" % "sbt-bintray" % "0.5.5")
