import spray.boilerplate.BoilerplatePlugin.Boilerplate

import java.util.Properties

val json4sVersion = "3.4.0"

val circeVersion = "0.5.0"

val akkaVersion = "2.4.8"

val playVersion = "2.5.4"

val appProperties = {
  val prop = new Properties()
  IO.load(prop, new File("project/version.properties"))
  prop
}

val assertNoApplicationConf = taskKey[Unit]("Makes sure application.conf isn't packaged")

val commonSettings = Seq(
  organization := "com.spingo",
  version := appProperties.getProperty("version"),
  scalaVersion := "2.11.8",
  crossScalaVersions := Seq("2.11.8"),
  resolvers ++= Seq(
    "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
    "SpinGo OSS" at "http://spingo-oss.s3.amazonaws.com/repositories/releases",
    "Sonatype Releases"  at "http://oss.sonatype.org/content/repositories/releases",
    "The New Motion Public Repo" at "http://nexus.thenewmotion.com/content/groups/public/"
  ),
  libraryDependencies ++= Seq(
    "com.chuusai" %%  "shapeless" % "2.3.1",
    "com.typesafe" % "config" % "1.3.0",
    "com.thenewmotion.akka" %% "akka-rabbitmq" % "2.3",
    "org.slf4j" % "slf4j-api" % "1.7.21",
    "ch.qos.logback" % "logback-classic" % "1.1.7" % "test",
    "org.scalatest" %% "scalatest" % "2.2.6" % "test",
    "com.spingo" %% "scoped-fixtures" % "1.0.0" % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion % "test"
  ),
  publishMavenStyle := true,
  publishTo := {
    val repo = if (version.value.trim.endsWith("SNAPSHOT")) "snapshots" else "releases"
    Some(repo at s"s3://spingo-oss/repositories/$repo")
  }
)

lazy val `op-rabbit` = (project in file(".")).
  settings(commonSettings: _*).
  settings(unidocSettings: _*).
  settings(
    description := "The opinionated Rabbit-MQ plugin",
    name := "op-rabbit").
  dependsOn(core).
  aggregate(core, `play-json`, airbrake, `akka-stream`, json4s, `spray-json`, circe)


lazy val core = (project in file("./core")).
  settings(Boilerplate.settings: _*).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-core"
  )


lazy val json4s = (project in file("./addons/json4s")).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-json4s",
    libraryDependencies ++= Seq(
      "org.json4s" %% "json4s-ast"     % json4sVersion,
      "org.json4s" %% "json4s-core"    % json4sVersion,
      "org.json4s" %% "json4s-jackson" % json4sVersion % "provided",
      "org.json4s" %% "json4s-native"  % json4sVersion % "provided")).
  dependsOn(core)

lazy val `play-json` = (project in file("./addons/play-json")).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-play-json",
    libraryDependencies += "com.typesafe.play" %% "play-json" % playVersion).
  dependsOn(core)

lazy val `spray-json` = (project in file("./addons/spray-json")).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-spray-json",
    libraryDependencies += "io.spray" %% "spray-json" % "1.3.2").
  dependsOn(core)

lazy val airbrake = (project in file("./addons/airbrake/")).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-airbrake",
    libraryDependencies += "io.airbrake" % "airbrake-java" % "2.2.8").
  dependsOn(core)

lazy val `akka-stream` = (project in file("./addons/akka-stream")).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-akka-stream",
    libraryDependencies ++= Seq(
      "com.timcharper"    %% "acked-streams" % "2.1.0",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion),
    unmanagedResourceDirectories in Test ++= Seq(
      (file(".").getAbsoluteFile) / "core" / "src" / "test" / "resources"),
    unmanagedSourceDirectories in Test ++= Seq(
      (file(".").getAbsoluteFile) / "core" / "src" / "test" / "scala" / "com" / "spingo" / "op_rabbit" / "helpers")).
  dependsOn(core)

lazy val circe = (project in file("./addons/circe")).
  settings(commonSettings: _*).
  settings(
    name := "op-rabbit-circe",
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )).
  dependsOn(core)
