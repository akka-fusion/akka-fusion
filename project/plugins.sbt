logLevel := Level.Warn

resolvers += "Typesafe repository".at("http://repo.typesafe.com/typesafe/releases/")

addSbtPlugin("com.github.mwz" % "sbt-sonar" % "1.6.0")
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.2.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-multi-jvm" % "0.4.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")
addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.9.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.25")
addSbtPlugin("io.github.jonas" % "sbt-paradox-material-theme" % "0.6.0")
//addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.0.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.5") // ALPN agent
addSbtPlugin("com.lightbend.akka.grpc" % "sbt-akka-grpc" % "0.7.1")
//addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.4.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.0.3")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.5")
addSbtPlugin("io.kamon" % "sbt-kanela-runner" % "2.0.2")
