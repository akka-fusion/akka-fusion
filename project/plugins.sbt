libraryDependencies += Defaults.sbtPluginExtra(
  "com.eed3si9n" % "sbt-assembly" % "1.1.0",
  (pluginCrossBuild / sbtBinaryVersion).value,
  (pluginCrossBuild / scalaBinaryVersion).value)

addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.6.1")
addSbtPlugin("com.lightbend.sbt" % "sbt-bill-of-materials" % "1.0.2")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.3")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.33")
// sbt-osgi 0.9.5 is available but breaks including jdk9-only classes
// sbt-osgi 0.9.6 is available but breaks populating akka-protobuf-v3
addSbtPlugin("com.typesafe.sbt" % "sbt-osgi" % "0.9.4")
addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.5.0")
addSbtPlugin("com.thoughtworks.sbt-api-mappings" % "sbt-api-mappings" % "3.0.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.3")
addSbtPlugin("io.spray" % "sbt-boilerplate" % "0.6.1")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox" % "0.9.2")
addSbtPlugin("com.wanari" % "sbt-paradox-diagrams" % "0.1.4")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.6.0")
addSbtPlugin("com.hpe.sbt" % "sbt-pull-request-validator" % "1.0.0")
addSbtPlugin("net.bzzt" % "sbt-reproducible-builds" % "0.30")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.10")
addSbtPlugin("com.lightbend.sbt" % "sbt-publish-rsync" % "0.2")

addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "2.1.1")
