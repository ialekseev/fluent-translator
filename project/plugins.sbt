resolvers ++= Seq(
  "Maven central http" at "http://repo1.maven.org/maven2",
  Resolver.url("sbt-plugin-releases http", url("http://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")