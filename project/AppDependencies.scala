import play.core.PlayVersion
import sbt.*

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % "8.4.0",
    "io.github.samueleresca"  %% "pekko-quartz-scheduler"     % "1.2.0-pekko-1.0.x"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % "8.4.0",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.6",
    "org.scalatest"           %% "scalatest"                  % "3.2.15",
    "org.playframework"       %% "play-test"                  % PlayVersion.current,
    "org.scalatestplus"       %% "mockito-4-6"                % "3.2.15.0",
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "org.scalamock"           %% "scalamock"                  % "5.2.0",
    "org.mockito"             %% "mockito-scala"              % "1.17.30"
  ).map(_ % "test, it")
}
