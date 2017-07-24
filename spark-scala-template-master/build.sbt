organization := "com.spark"

name := "spark-scala"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-unchecked", "-deprecation")

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" % "spark-streaming_2.11" % "2.1.0",
  "org.apache.bahir" %% "spark-streaming-mqtt" % "2.1.0",
  "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "latest.integration",
  "org.json4s" %% "json4s-native" % "latest.integration",
  "org.mongodb" %% "casbah" % "3.1.1",
  "org.facebook4j" % "facebook4j-core" % "2.4.9",
  "javax.mail" % "javax.mail-api" % "1.5.6",
  "javax" % "javaee-api" % "7.0",
  "com.googlecode.json-simple" % "json-simple" % "1.1.1"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

