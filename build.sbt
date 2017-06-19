name := "AkkaTemplateApp"

version := "1.0"

scalaVersion := "2.12.2"

organization := "com.is"

libraryDependencies ++= {
  val akkaVersion = "2.5.2"
  val akkaHttpVersion = "10.0.7"
  val logbackVersion = "1.2.3"
  val scalaTestVersion = "3.0.3"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "ch.qos.logback" % "logback-classic" % logbackVersion,

    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test
  )
}
        