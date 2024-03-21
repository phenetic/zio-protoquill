resolvers += Classpaths.sbtPluginReleases

resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "phenetic repository" at "https://github.com/phenetic/"


addDependencyTreePlugin

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "2.0.11")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.2")
//addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.5.12")

addSbtPlugin("com.github.sbt" % "sbt-dynver"          % "5.0.1")
addSbtPlugin("com.github.sbt" % "sbt-git"             % "2.0.1")
addSbtPlugin("com.codecommit" % "sbt-github-packages" % "0.5.3")
