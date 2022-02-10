package uk.gov.hmrc.onestopshopforexrates.connectors

import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}
import uk.gov.hmrc.onestopshopforexrates.config.DesConfig
import uk.gov.hmrc.onestopshopforexrates.model.core.CoreExchangeRateRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject()(
                              httpClient: HttpClient,
                              desConfig: DesConfig
                            )(implicit ec: ExecutionContext) extends Logging {
  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()
  private val headers: Seq[(String, String)] = desConfig.desHeaders

  private def url = s"${desConfig.baseUrl}oss/referencedata/v1/exchangerate"

  def postLast5DaysToCore(rates: CoreExchangeRateRequest): Future[HttpResponse] = {
    httpClient.POST[CoreExchangeRateRequest, HttpResponse](
      url,
      rates,
      headers = headers
    )
  }

}
