import play.core.PlayVersion
import sbt.*

object AppDependencies {

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % "9.5.0",
    "io.github.samueleresca"  %% "pekko-quartz-scheduler"     % "1.2.0-pekko-1.0.x"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % "9.5.0",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.8",
    "org.scalatest"           %% "scalatest"                  % "3.2.19",
    "org.playframework"       %% "play-test"                  % PlayVersion.current,
    "org.scalatestplus"       %% "scalacheck-1-15"            % "3.2.11.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.1",
    "org.scalamock"           %% "scalamock"                  % "6.0.0",
    "org.scalatestplus"       %% "mockito-4-11"               % "3.2.18.0"
  ).map(_ % "test, it")
}
