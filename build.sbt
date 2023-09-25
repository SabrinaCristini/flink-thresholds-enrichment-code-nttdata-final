ThisBuild / resolvers ++= Seq("Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
  Resolver.mavenLocal)

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

val flinkVersion = "1.16.0"
val jacksonVersion = "2.9.8"
val log4jVersion = "2.19.0"
val slf4jVersion = "1.7.32"
val jexlVersion = "3.2.1"


val flinkDependencies = Seq(

  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided",
  "org.apache.flink" % "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" % "flink-connector-kafka" % flinkVersion,
  "com.jayway.jsonpath" % "json-path" % "2.6.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.5",
  "org.slf4j" % "slf4j-api" % slf4jVersion,
  "org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
  "org.apache.flink" % "flink-json" % "1.16.0",
  "com.typesafe.play" %% "play-json" % "2.9.2",
  "org.json4s" %% "json4s-native" % "4.0.3",
  "org.apache.commons" % "commons-jexl3" % jexlVersion
)

assemblyMergeStrategy in assembly := {
  case PathList("jackson-annotations-2.13.1.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-core-2.13.1.jar", xs @ _*) => MergeStrategy.last
  case PathList("jackson-databind-2.13.1.jar", xs @ _*) => MergeStrategy.last
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}



lazy val root = (project in file("."))
  .settings(
    name := "enrichment",
  )
  .settings(libraryDependencies ++= flinkDependencies)

// make run command include the provided dependencies
Compile / run := Defaults.runTask(
  Compile / fullClasspath,
  Compile / run / mainClass,
  Compile / run / runner
  
).evaluated

// exclude Scala library from assembly
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)
