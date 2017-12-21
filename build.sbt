
name := "icantbelieveitsnotdelius"

organization := "gov.uk.justice.digital"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.5.1",
  "org.clapper" %% "grizzled-slf4j" % "1.3.0",
  "net.codingwell" %% "scala-guice" % "4.1.0",
  "com.typesafe.akka" %% "akka-http" % "10.0.5",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.17",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.5.1" % "test"
)

assemblyJarName in assembly := "icbind-" + version.value + ".jar"
