package uk.gov.hmrc.onestopshopforexrates.scheduler.jobs


import akka.actor.ActorSystem
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.onestopshopforexrates.scheduler.ScheduledJob
import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesService

import javax.inject.Inject

class RetrieveAndSendForexDataJob@Inject()(val config: Configuration,
                                           val service: ExchangeRatesService,
                                           val applicationLifecycle: ApplicationLifecycle
                                          ) extends ScheduledJob {

  val jobName: String           = "ProcessUploadedFilesJob"
  val actorSystem: ActorSystem  = ActorSystem(jobName)
  val scheduledMessage: ProcessUploadedFilesClass = ProcessUploadedFilesClass(service)

  schedule

}
