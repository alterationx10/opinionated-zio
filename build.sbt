ThisBuild / scalaVersion     := "3.3.0"
ThisBuild / version          := "0.0.1-SNAPSHOT"
ThisBuild / organization     := "com.alterationx10"
ThisBuild / organizationName := "Alterationx10"

lazy val opinions = (project in file("opinions"))
  .settings(
    name := "opinionated-zio",
    libraryDependencies ++= Dependencies.opinions,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
