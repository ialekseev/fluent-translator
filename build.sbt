name := "microsoft-translator-scala-api"
organization := "com.smartelk"
version := "1.0"

scalaVersion := "2.11.7"
scalacOptions := Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.1.4",
  "org.scalaz" %% "scalaz-concurrent" % "7.1.4",

  "org.scalatest" %%  "scalatest"   % "2.2.1" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)