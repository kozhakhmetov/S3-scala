name := "AWS"

version := "0.1"

scalaVersion := "2.12.8"

lazy val slickVersion = "3.2.1"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  // AWS
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.531",

  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % Test,
  "com.typesafe.akka" %% "akka-slf4j" % "2.5.21",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-http"   % "10.1.7",
  // JSON support
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.7",
  "com.typesafe.akka" %% "akka-stream" % "2.5.21",

  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion

)
