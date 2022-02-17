package uk.gov.hmrc.onestopshopforexrates.base

import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesService

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class FakeExchangeRateService extends ExchangeRatesService {
  override val jobName: String = "RetrieveAndSendForexDataJob"

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] = Future.successful(true)
}
