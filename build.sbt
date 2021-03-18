name := """scala-play-angular-seed"""

version := "1.0-SNAPSHOT"

lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.0",
)

lazy val root = (project in file(".")).settings(commonSettings: _*).
  settings(
    name := "TimeManagement",
    watchSources ++= (baseDirectory.value / "ui/src" ** "*").get
  ).enablePlugins(PlayScala, AssemblyPlugin)

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.url("bintray-sbt-plugins", url("http://dl.bintray.com/sbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)
resolvers in Global ++= Seq(
  "Sbt plugins"                   at "https://dl.bintray.com/sbt/sbt-plugin-releases",
  "Maven Central Server"          at "http://repo1.maven.org/maven2",
  "TypeSafe Repository Releases"  at "http://repo.typesafe.com/typesafe/releases/",
  "TypeSafe Repository Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/"
)

scalaVersion := "2.12.8"

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.2" % Test,
  "com.typesafe.slick" %% "slick" % "3.3.2",
  "org.xerial" % "sqlite-jdbc" % "3.34.0",
  "com.typesafe.play" %% "play-mailer" % "8.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "8.0.1",
)

libraryDependencies += "net.sourceforge.htmlcleaner" % "htmlcleaner" % "2.24"
libraryDependencies += "org.xhtmlrenderer" % "flying-saucer-pdf" % "9.1.20"

scalaVersion in ThisBuild := "2.11.7"

//https://stackoverflow.com/questions/50636477/play-framework-sbt-assembly-jar-running-error-no-root-server-path-supplied
