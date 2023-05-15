ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"


lazy val root = (project in file(".")).settings(
    name := "cats-project-pps-21-22",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "org.typelevel" %% "cats-effect" % "3.4.11",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
    ),

  )