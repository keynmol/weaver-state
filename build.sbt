scalaVersion := "2.13.4"


libraryDependencies += "com.disneystreaming" %% "weaver-cats" % "0.6.0-M6"
testFrameworks += new TestFramework("weaver.framework.CatsEffect")

libraryDependencies += "com.softwaremill.diffx" %% "diffx-core" % "0.4.0"

addCommandAlias("ci", ";clean;test")
