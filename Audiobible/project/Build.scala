import sbt._
import sbt.Keys._
import android.Keys._
import android.Dependencies.apklib
import android.Dependencies.AutoLibraryProject

object ThisBuild extends Build {

  lazy val modelProj = Project(
    id = "modelProj",
    base = file("./modelProj"),
    settings =  Defaults.defaultSettings ++ Seq (
      libraryDependencies += "com.typesafe.slick" %% "slick" % "2.0.0",
      libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.0" % "test",
      libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.1.RC1" % "test",
      scalaVersion := "2.10.2"
    )
  )

  lazy val root = Project(
    id = "AudioBible",
    base = file("."),
    settings =  Defaults.defaultSettings ++ Settings.androidStd
  ).dependsOn( modelProj )



  object Settings {

    lazy val androidStd = android.Plugin.androidBuild ++ Seq (
      name <<= baseDirectory.apply(_.getName),
      organization := "com.agapep",
      version := "1.0",
      versionCode := Some(1),
      scalaVersion := "2.10.0",
//      crossScalaVersions := Seq("2.9.1","2.9.2","2.10.0"),
      platformTarget in Android := "android-19",
      minSdkVersion := 8,
      debugIncludesTests in Android := false,
      publishArtifact in packageDoc := false
      
      //~ localAars in Android <+= (baseDirectory) { bd =>
        //~ bd.getAbsoluteFile / "project-libs/ExFilePicker/"
      //~ }
    )
  }
}
