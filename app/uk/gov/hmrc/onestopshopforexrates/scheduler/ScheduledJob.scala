package uk.gov.hmrc.onestopshopforexrates.scheduler

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.Logging
import uk.gov.hmrc.onestopshopforexrates.scheduler.SchedulingActor.ScheduledMessage

import scala.concurrent.Future

trait ScheduledJob extends Logging {

  val scheduledMessage: ScheduledMessage[_]
  val config: Configuration
  val actorSystem: ActorSystem
  val jobName: String

  val applicationLifecycle: ApplicationLifecycle

  lazy val scheduler: QuartzSchedulerExtension = QuartzSchedulerExtension(actorSystem)

  lazy val schedulingActorRef: ActorRef = actorSystem.actorOf(SchedulingActor.props)

  lazy val enabled: Boolean = config.getOptional[Boolean](s"schedules.$jobName.enabled").getOrElse(false)

  lazy val description: Option[String] = config.getOptional[String](s"schedules.$jobName.description")

  lazy val expression: String = config.getOptional[String](s"schedules.$jobName.expression") map (_.replaceAll("_", " ")) getOrElse ""

  lazy val schedule: Unit = {
    (enabled, expression.nonEmpty) match {
      case (true, true) =>
        scheduler.createSchedule(jobName, description, expression)
        scheduler.schedule(jobName, schedulingActorRef, scheduledMessage)
        logger.info(s"Scheduler for $jobName has been started")
      case (true, false) =>
        logger.info(s"Scheduler for $jobName is disabled as there is no quartz expression")
      case (false, _) =>
        logger.info(s"Scheduler for $jobName is disabled by configuration")
    }
  }

  applicationLifecycle.addStopHook { () =>
    Future.successful(scheduler.cancelJob(jobName))
    Future.successful(scheduler.shutdown(waitForJobsToComplete = false))
  }
}

