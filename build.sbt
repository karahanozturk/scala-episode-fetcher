import play.PlayScala
import templemore.sbt.cucumber.CucumberPlugin

name := """episode-fetcher"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.4"

val cucumberSettings = CucumberPlugin.cucumberSettings ++
  Seq(
    CucumberPlugin.cucumberFeaturesLocation := "./test/features",
    CucumberPlugin.cucumberStepsBasePackage := "features.steps")

lazy val root = Project ("episode-fetcher", file ("."), settings = Defaults.coreDefaultSettings ++ cucumberSettings)
  .enablePlugins(PlayScala)


resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype-Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.google.inject" % "guice" % "3.0",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "joda-time" % "joda-time" % "2.4",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "com.github.tomakehurst" % "wiremock" % "1.46" % "test",
  "info.cukes" % "cucumber-scala_2.10" % "1.1.8" % "test",
  "info.cukes" % "cucumber-junit" % "1.1.8" % "test",
  "info.cukes" % "cucumber-picocontainer" % "1.1.8" % "test"
)