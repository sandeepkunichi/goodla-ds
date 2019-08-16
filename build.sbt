import sbt._

name := "goodla-ds"

version := "0.1"

scalaVersion := "2.11.8"

mainClass in Compile := Some("com.goodla.datastore.DataStoreServer")
enablePlugins(JavaAppPackaging)

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.5",
  "com.typesafe.akka" %% "akka-http-testkit" % "10.0.5",
  "com.typesafe.akka" %% "akka-stream" % "2.4.17",
  "com.typesafe.akka" %% "akka-actor" % "2.4.17",
  "org.scalatest" %% "scalatest" % "2.2.5" % "test",
  "com.google.inject" % "guice" % "4.1.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "ch.qos.logback" % "logback-classic" % "1.1.2",
  "com.github.pureconfig" %% "pureconfig" % "0.10.1",
  "com.hazelcast" %% "hazelcast-scala" % "3.7.2" withSources(),
  "com.hazelcast" % "hazelcast" % "3.7.2" withSources()
)


