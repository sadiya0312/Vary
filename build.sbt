name := "varys_framework"

ThisBuild / organization := "com.sadhiya.vays"
ThisBuild / version := "0.1"
ThisBuild / scalaVersion := "2.13.4"

val excludeNetty = ExclusionRule(organization = "org.jboss.netty")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" %"2.6.12" excludeAll (excludeNetty)
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-slf4j
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.6.12" excludeAll (excludeNetty)
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.0"
// https://mvnrepository.com/artifact/com.google.guava/guava
libraryDependencies += "com.google.guava" % "guava" % "30.1-jre"
// https://mvnrepository.com/artifact/org.scala-lang.modules/scala-xml
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.3.0"
// https://mvnrepository.com/artifact/org.eclipse.jetty/jetty-server
libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "11.0.0"
// https://mvnrepository.com/artifact/net.liftweb/lift-json
libraryDependencies += "net.liftweb" %% "lift-json" % "3.4.3"
// https://mvnrepository.com/artifact/io.netty/netty-all
libraryDependencies += "io.netty" % "netty-all" % "4.1.58.Final"
// https://mvnrepository.com/artifact/com.typesafe.akka/akka-remote
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.6.12" excludeAll (excludeNetty)
// https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
libraryDependencies += "com.google.protobuf" % "protobuf-java" % "3.14.0"
// https://mvnrepository.com/artifact/log4j/log4j
libraryDependencies += "log4j" % "log4j" % "1.2.17"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.30"
// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.30" % Test
// https://mvnrepository.com/artifact/org.fusesource/sigar
libraryDependencies += "org.fusesource" % "sigar" % "1.6.4"
// https://mvnrepository.com/artifact/com.esotericsoftware/kryo
libraryDependencies += "com.esotericsoftware" % "kryo" % "5.0.3"

// https://mvnrepository.com/artifact/io.altoo/akka-kryo-serialization
libraryDependencies += "io.altoo" %% "akka-kryo-serialization" % "1.0.0"




