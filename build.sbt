scalaVersion := "2.13.4"

libraryDependencies += "com.disneystreaming" %% "weaver-cats" % "0.6.0-M6"
testFrameworks += new TestFramework("weaver.framework.CatsEffect")

libraryDependencies += "com.softwaremill.diffx" %% "diffx-core" % "0.4.0"

addCommandAlias("ci", ";clean;test")

libraryDependencies ++= Seq(
  "com.github.julien-truffaut" %% "monocle-core"  % "2.1.0",
  "com.github.julien-truffaut" %% "monocle-macro" % "2.1.0"
)

scalacOptions in Global += "-Ymacro-annotations"
