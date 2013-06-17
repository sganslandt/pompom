import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "pompom"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    "com.github.nscala-time" %% "nscala-time" % "0.4.0",
    "org.eligosource" %% "eventsourced-core" % "0.5.0",
    "org.eligosource" %% "eventsourced-journal-leveldb" % "0.5.0",
    "org.eligosource" %% "eventsourced-journal-inmem" % "0.5.0"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
    resolvers += "Eligosource Releases" at "http://repo.eligotech.com/nexus/content/repositories/eligosource-releases"
  )

}
