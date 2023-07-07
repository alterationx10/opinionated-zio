import sbt._

object Dependencies {

  object Versions {
    val zio       = "2.0.15"
    val zioConfig = "3.0.7"
    val zioMock   = "1.0.0-RC9"
  }

  val opinions: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio"                 % Versions.zio,
    "dev.zio" %% "zio-streams"         % Versions.zio,
    "dev.zio" %% "zio-test"            % Versions.zio,
    "dev.zio" %% "zio-test-sbt"        % Versions.zio,
    "dev.zio" %% "zio-test-magnolia"   % Versions.zio     % Test,
    "dev.zio" %% "zio-mock"            % Versions.zioMock % Test,
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  val testOpinions: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio-test"          % Versions.zio,
    "dev.zio" %% "zio-test-sbt"      % Versions.zio,
    "dev.zio" %% "zio-test-magnolia" % Versions.zio,
    "dev.zio" %% "zio-mock"          % Versions.zioMock
  )

}
