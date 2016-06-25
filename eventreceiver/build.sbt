organization := "com.lucianomolinari.ap"

name := "eventreceiver"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
{
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
}

assemblyJarName in assembly := "event-receiver.jar"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "0.10.0.0",
  "com.typesafe.play" %% "play-json" % "2.5.4",
  "com.typesafe.akka" %% "akka-http-core" % "2.4.7",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % "2.4.7"
)