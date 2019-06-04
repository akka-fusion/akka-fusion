// Comment to get more information during initialization
logLevel := Level.Warn

//resolvers += Resolver.sbtPluginRepo("releases")

//resolvers += Resolver.bintrayRepo("kamon-io", "sbt-plugins")
resolvers += "Typesafe repository".at("http://repo.typesafe.com/typesafe/releases/")

//addSbtPlugin("org.scala-js"      % "sbt-scalajs"                % "0.6.26")
addSbtPlugin("de.heikoseeberger" % "sbt-header"                 % "5.2.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-multi-jvm"              % "0.4.0")
addSbtPlugin("com.eed3si9n"      % "sbt-assembly"               % "0.14.9")
addSbtPlugin("net.virtual-void"  % "sbt-dependency-graph"       % "0.9.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-native-packager"        % "1.3.21")
addSbtPlugin("io.github.jonas"   % "sbt-paradox-material-theme" % "0.6.0")
addSbtPlugin("com.dwijnand"      % "sbt-dynver"                 % "3.3.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"                    % "1.0.0")
addSbtPlugin("io.kamon"          % "sbt-aspectj-runner"         % "1.1.1")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent"              % "0.1.5")
addSbtPlugin("com.thesamet"      % "sbt-protoc"                 % "0.99.20")
addSbtPlugin("com.geirsson"      % "sbt-scalafmt"               % "1.5.1")
//addSbtPlugin("org.scalameta"     % "sbt-scalafmt"               % "2.0.0")
//addSbtPlugin("ch.epfl.scala"     % "sbt-scalafix"               % "0.9.5")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.8.4"
