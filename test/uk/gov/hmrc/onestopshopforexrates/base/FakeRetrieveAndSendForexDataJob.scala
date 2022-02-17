package uk.gov.hmrc.onestopshopforexrates.base

import akka.actor.ActorSystem
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.onestopshopforexrates.scheduler.SchedulingActor
import uk.gov.hmrc.onestopshopforexrates.scheduler.SchedulingActor.{RetrieveAndSendExchangeRatesClass, ScheduledMessage}
import uk.gov.hmrc.onestopshopforexrates.scheduler.jobs.RetrieveAndSendForexDataJob

import javax.inject.Inject

class FakeRetrieveAndSendForexDataJob @Inject()(val config: Configuration,
                                               val service: FakeExchangeRateService,
                                               val applicationLifecycle: ApplicationLifecycle
                                              ) extends RetrieveAndSendForexDataJob {
  override val jobName: String = "RetrieveAndSendForexDataJob"
  override val scheduledMessage: SchedulingActor.ScheduledMessage[_] = RetrieveAndSendExchangeRatesClass(service)
  override val actorSystem: ActorSystem = ActorSystem(jobName)
}
