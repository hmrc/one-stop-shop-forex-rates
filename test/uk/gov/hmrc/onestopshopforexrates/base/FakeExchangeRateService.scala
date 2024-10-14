package uk.gov.hmrc.onestopshopforexrates.base

import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesService

import scala.concurrent.Future

class FakeExchangeRateService extends ExchangeRatesService {
  override val jobName: String = "RetrieveAndSendForexDataJob"

  override def invoke: Future[Boolean] = Future.successful(true)
}
