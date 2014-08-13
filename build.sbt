name := """episode-fetcher"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.google.inject" % "guice" % "3.0",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "joda-time" % "joda-time" % "2.4",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.github.tomakehurst" % "wiremock" % "1.46" % "test"
)

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"