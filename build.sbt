name := "fluent-translator"
organization := "com.smartelk"
version := "2.1.0"
scalaVersion := "2.11.7"
scalacOptions := Seq("-deprecation", "-feature")
resolvers ++= Seq(
  "Maven central http" at "http://repo1.maven.org/maven2"
)
libraryDependencies ++= Seq(
  "com.typesafe.akka"   %%  "akka-actor"  % "2.3.9",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.3",
  "org.json4s" %% "json4s-native" % "3.3.0",
  "com.typesafe.akka"   %%  "akka-testkit"  % "2.3.9" % "test",
  "org.scalatest" %%  "scalatest"   % "2.2.1" % "test",
  "org.mockito" % "mockito-core" % "1.10.19" % "test"
)

bintrayOrganization := Some("smartelk")
bintrayVcsUrl := Some("https://github.com/SmartElk/fluent-translator.git")
bintrayPackageLabels := Seq("scala", "translator")
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))