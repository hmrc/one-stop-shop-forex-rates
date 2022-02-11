/*
 * Copyright 2022 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.onestopshopforexrates.services

import com.google.inject.Inject
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.onestopshopforexrates.config.AppConfig
import uk.gov.hmrc.onestopshopforexrates.scheduler.ScheduledService

import scala.concurrent.{ExecutionContext, Future}

class RetrieveAndSendService @Inject()(appConfig: AppConfig,
                                       exchangeRatesService: ExchangeRatesService) extends ScheduledService[Boolean] with Logging {

  override val jobName: String = "RetrieveAndSendForexDataJob"

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] = {
      logger.info(s"[$jobName Scheduled Job Started]")

      implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

      exchangeRatesService.retrieveAndSendToCore().map {
        case Right(_) => logger.info(s"[$jobName ran successfully]") true
        case Left(error) => logger.info(s"[Error when running $jobName: ${error.errorMessage}]") false
      }
  }

}
