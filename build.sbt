name := "scala-practice"

version := "0.1"

scalaVersion := "2.13.8"

val AkkaVersion = "2.6.19"
//libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.11"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.11" % "test"

//Akka
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % AkkaVersion
//libraryDependencies += "org.json4s" % "json4s-native_2.11" % "4.0.5"
libraryDependencies += "org.json4s" % "json4s-jackson-core_2.13" % "4.0.0"