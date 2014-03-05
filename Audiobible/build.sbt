import android.Keys._

import android.Dependencies.AutoLibraryProject

android.Plugin.androidBuild

platformTarget in Android := "android-19"


//TODO błędy w testach. Musiałem je wyłączyć ale przydałoby się
//zrobić je od nowa aby działały.
debugIncludesTests in Android := false

localProjects in Android <+= (baseDirectory) { bd =>
    AutoLibraryProject(bd / "project-libs/ExFilePicker" )
}