import sbt._

organization := "de.htwg"

name := "Battleship-play"

version := "1.0-SNAPSHOT"

lazy val Battleship = project.in(file("lib/Battleship"))

lazy val root = project.in(file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .aggregate(Battleship)
  .dependsOn(Battleship)

scalaVersion := "2.11.6"

resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  javaWs,
  "com.feth" %% "play-authenticate" % "0.7.0",
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.4.1",
  "junit" % "junit" % "4.12" % "test"
)

herokuAppName in Compile := "battleship-play"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := false