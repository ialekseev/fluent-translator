name := "microsoft-translator-scala-api"
organization := "com.smartelk"
version := "0.1.1"
scalaVersion := "2.11.7"
scalacOptions := Seq("-deprecation", "-feature")
resolvers ++= Seq(
  "Maven central http" at "http://repo1.maven.org/maven2"
)
libraryDependencies ++= Seq(
  "com.typesafe.akka"   %%  "akka-actor"  % "2.3.9",
  "org.scalaj" %% "scalaj-http" % "2.2.0",
  "org.json4s" % "json4s-native_2.11" % "3.3.0",
  "com.typesafe.akka"   %%  "akka-testkit"  % "2.3.9" % "test",
  "org.scalatest" %%  "scalatest"   % "2.2.1" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

bintrayOrganization in bintray := Some("smartelk")
bintrayVcsUrl := Some("git@github.com:SmartElk/microsoft-translator-scala-api.git")
bintrayPackageLabels := Seq("scala", "microsoft", "translator", "api")
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
