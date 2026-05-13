import play.core.PlayVersion
import sbt.*

object AppDependencies {

  private val pekkoVersion = "1.6.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % "10.7.0",
    ("io.github.samueleresca"  %% "pekko-quartz-scheduler"     % "1.3.0-pekko-1.1.x")
      .exclude("com.mchange", "c3p0")
      .exclude("com.mchange", "mchange-commons-java"),
    "org.apache.pekko"        %% "pekko-protobuf-v3"           % pekkoVersion,
    "org.apache.pekko"        %% "pekko-serialization-jackson" % pekkoVersion,
    "org.apache.pekko"        %% "pekko-stream"                % pekkoVersion,
    "org.apache.pekko"        %% "pekko-actor-typed"           % pekkoVersion,
    "org.apache.pekko"        %% "pekko-slf4j"                 % pekkoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % "10.7.0",
    "com.vladsch.flexmark"    %  "flexmark-all"               % "0.64.8",
    "org.scalatest"           %% "scalatest"                  % "3.2.19",
    "org.playframework"       %% "play-test"                  % PlayVersion.current,
    "org.scalatestplus"       %% "scalacheck-1-17"            % "3.2.18.0",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "7.0.2",
    "org.scalamock"           %% "scalamock"                  % "7.5.5",
    "org.scalatestplus"       %% "mockito-4-11"               % "3.2.18.0"
  ).map(_ % "test, it")
}
