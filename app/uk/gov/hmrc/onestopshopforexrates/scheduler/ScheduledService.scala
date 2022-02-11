package uk.gov.hmrc.onestopshopforexrates.scheduler

import scala.concurrent.{Future, ExecutionContext => ExC}

trait ScheduledService[R] {
  val jobName: String
  def invoke(implicit ec : ExC) : Future[R]
}
