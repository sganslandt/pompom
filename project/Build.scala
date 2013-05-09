import sbt._
import Keys._
import play.Project._
import net.litola.SassPlugin

object ApplicationBuild extends Build {

    val appName         = "pompom"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      // Add your project dependencies here,
      "com.github.nscala-time" %% "nscala-time" % "0.4.0"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
      // Add your own project settings here
      SassPlugin.sassSettings:_*
    )

}
