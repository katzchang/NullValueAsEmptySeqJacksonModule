name := "scala scalatest template"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.1.3"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"
