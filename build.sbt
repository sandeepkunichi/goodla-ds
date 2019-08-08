import sbt._

name := "goodla-ds"

version := "0.1"

scalaVersion := "2.11.7"

resolvers += Resolver.jcenterRepo

libraryDependencies += "com.hazelcast" %% "hazelcast-scala" % "3.7.2" withSources()
libraryDependencies += "com.hazelcast" % "hazelcast" % "3.7.2" withSources()

mainClass in Compile := Some("com.goodla.datastore.Server")