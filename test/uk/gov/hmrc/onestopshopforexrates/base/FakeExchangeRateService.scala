package uk.gov.hmrc.onestopshopforexrates.base

import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesService

import scala.concurrent.{ExecutionContext, Future}

class FakeExchangeRateService extends ExchangeRatesService {
  override val jobName: String = "RetrieveAndSendForexDataJob"

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] = Future.successful(true)
}
