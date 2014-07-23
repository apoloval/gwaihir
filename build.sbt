name := "gwaihir"

organization in ThisBuild := "gwaihir"

scalaVersion in ThisBuild := "2.11.1"

resolvers in ThisBuild ++= Seq(
  DefaultMavenRepository,
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.url("Play", url("http://download.playframework.org/ivy-releases/"))(Resolver.ivyStylePatterns)
)

libraryDependencies in ThisBuild ++= Seq(
  "com.squants"  %% "squants"  % "0.4.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.0" % "test",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

