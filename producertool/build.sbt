organization := "com.lucianomolinari.ap"

name := "producertool"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.7"

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("META-INF", xs @ _*) => MergeStrategy.discard
    case x => MergeStrategy.first
  }
}

assemblyJarName in assembly := "producer-tool.jar"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "0.10.0.0",
  "org.slf4j" % "slf4j-simple" % "1.7.21"
)
