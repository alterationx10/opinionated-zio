ThisBuild / scalaVersion           := "3.3.0"
ThisBuild / organization           := "com.alterationx10"
ThisBuild / organizationName       := "Alterationx10"
ThisBuild / organizationHomepage   := Some(url("https://alterationx10.com"))
ThisBuild / homepage               := Some(
  url("https://github.com/alterationx10/opinionated-zio")
)
ThisBuild / description            := "A collection of opinionated extensions and helpers around ZIO and vanilla Scala."
ThisBuild / licenses               := List(
  "Apache 2" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / versionScheme          := Some("early-semver")
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository     := "https://s01.oss.sonatype.org/service/local"
ThisBuild / developers             := List(
  Developer(
    id = "alterationx10",
    name = "Mark Rudolph",
    email = "mark@scala.works",
    url = url("https://alterationx10.com/")
  )
)

lazy val root = (project in file("."))
  .settings(
    publish / skip := true
  )
  .aggregate(opinions, testOpinions)

lazy val opinions = (project in file("opinions"))
  .settings(
    name := "opinionated-zio",
    libraryDependencies ++= Dependencies.opinions,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

lazy val testOpinions = (project in file("test-opinions"))
  .settings(
    name := "opinionated-zio-test",
    libraryDependencies ++= Dependencies.testOpinions,
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
  .dependsOn(opinions)
