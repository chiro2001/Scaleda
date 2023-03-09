import org.jetbrains.sbtidea.Keys._
import scalapb.compiler.Version.scalapbVersion

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / intellijPluginName := "Scaleda"
ThisBuild / intellijPlatform := IntelliJPlatform.IdeaCommunity
ThisBuild / intellijBuild := "221.5787.30"
ThisBuild / githubWorkflowJavaVersions := Seq(JavaSpec.temurin("11"))
Global / intellijAttachSources := true

val junitInterfaceVersion = "0.11"
val jacksonVersion = "2.13.4"

lazy val scaleda = project.in(file(".")).enablePlugins(SbtIdeaPlugin).settings(
  version := "0.0.1-SNAPSHOT",
  // Compile / scalaSource := baseDirectory.value / "src",
  // Test / scalaSource := baseDirectory.value / "test",
  // Compile / resourceDirectory := baseDirectory.value / "resources",
  Global / javacOptions ++= Seq("-source", "11", "-target", "11"),
  Global / scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings",
  ),
  ideBasePackages := Seq("top.criwits.scaleda"),
  intellijPlugins := Seq("com.intellij.properties", "com.intellij.java", "com.intellij.java-i18n",
    // "antlr4-intellij-plugin-sample"
  ).map(_.toPlugin),
  libraryDependencies ++= Seq(
    "com.novocode" % "junit-interface" % junitInterfaceVersion % Test,
    "org.antlr" % "antlr4-intellij-adaptor" % "0.1",
    "org.antlr" % "antlr4" % "4.11.1",
    "org.antlr" % "antlr4-runtime" % "4.11.1",
    "io.circe" %% "circe-yaml" % "0.14.2",
    "com.github.scopt" %% "scopt" % "4.1.0",
    "ch.qos.logback" % "logback-classic" % "1.4.5",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    "org.scalactic" %% "scalactic" % "3.2.14",
    "org.scalatest" %% "scalatest" % "3.2.14" % "test",
    // for logger
    "com.lihaoyi" %% "sourcecode" % "0.2.8",
    // for color print
    "com.lihaoyi" %% "fansi" % "0.3.1",
  ),
  packageLibraryMappings := Seq.empty, // allow scala-library
  patchPluginXml := pluginXmlOptions { xml =>
    xml.version = version.value
  },
  assembly / assemblyJarName := "scaleda.jar",
  assembly / mainClass := Some("top.criwits.scaleda.kernel.shell.ScaledaShellMain"),
  Compile / PB.targets := Seq(
    scalapb.gen() -> (Compile / sourceManaged).value
  ),
  libraryDependencies ++= Seq(
    "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
    "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
  ),
  // https://mvnrepository.com/artifact/com.hubspot.jinjava/jinjava
  libraryDependencies += "com.hubspot.jinjava" % "jinjava" % "2.6.0",
  // https://mvnrepository.com/artifact/com.google.guava/guava
  libraryDependencies += "com.google.guava" % "guava" % "31.1-jre",
  // https://mvnrepository.com/artifact/commons-io/commons-io
  libraryDependencies += "commons-io" % "commons-io" % "2.11.0",
  // https://mvnrepository.com/artifact/log4j/log4j
  libraryDependencies += "log4j" % "log4j" % "1.2.17",
  libraryDependencies += "org.scala-lang.modules" %% "scala-async" % "1.0.1",
  libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided,
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala
  libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-yaml
  libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion,
  // https://mvnrepository.com/artifact/com.fasterxml.jackson.dataformat/jackson-dataformat-xml
  libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-xml" % jacksonVersion,
  scalacOptions += "-Xasync",
  // https://mvnrepository.com/artifact/com.github.serceman/jnr-fuse
  libraryDependencies += "com.github.serceman" % "jnr-fuse" % "0.5.7",
  // https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc
  libraryDependencies += "org.xerial" % "sqlite-jdbc" % "3.41.0.0",
  // https://mvnrepository.com/artifact/com.auth0/java-jwt
  libraryDependencies += "com.auth0" % "java-jwt" % "4.3.0",

    assembly / assemblyMergeStrategy := {
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".properties" => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".xml" => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".types" => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".class" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case "unwanted.txt" => MergeStrategy.discard
    case x =>
      val oldStrategy = (assembly / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)
