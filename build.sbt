
name := "icantbelieveitsnotdelius"

organization := "gov.uk.justice.digital"

version := "0.0.1"

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.5.3",
  "org.clapper" %% "grizzled-slf4j" % "1.3.2",
  "net.codingwell" %% "scala-guice" % "4.1.1",
  "com.typesafe.akka" %% "akka-http" % "10.0.11",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.20",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.12.0" % "test"
)

assemblyJarName in assembly := "icbind-" + version.value + ".jar"
