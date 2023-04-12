val jettyVersion = "9.4.12.v20180830"

organization := "com.github.zwrss"

name := "agents-party-game"

version := "0.1"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-server" % jettyVersion,
  "org.eclipse.jetty" % "jetty-servlet" % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-api" % jettyVersion,
  "org.eclipse.jetty.websocket" % "websocket-server" % jettyVersion,
  "com.typesafe.play" %% "play-json" % "2.6.10"
)

enablePlugins(JavaAppPackaging)
