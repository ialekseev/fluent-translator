resolvers ++= Seq(
  "Maven central" at "http://repo1.maven.org/maven2",
  Resolver.url("fix-sbt-plugin-releases", url("http://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")