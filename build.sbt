import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import scoverage.ScoverageKeys

val appName = "one-stop-shop-forex-rates"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion                     := 0,
    scalaVersion                     := "2.13.14",
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test,

    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;" +
      ".*Routes.*;" + ".*AppConfig.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 78,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings): _*)
  .configs(Test)
  .settings(inConfig(Test)(testSettings): _*)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(PlayKeys.playDefaultPort := 10199)
  .settings(scalacOptions+="-Wconf:src=routes/.*:s")


lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test" / "generators"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork := true,
  javaOptions ++= Seq(
    "-Dlogger.resource=logback-it.xml"
  )
)

lazy val testSettings = Defaults.testSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "test"
  ),
  parallelExecution := false,
  fork := true,
  javaOptions ++= Seq(
    "-Dlogger.resource=logback-test.xml"
  )
)
