/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.onestopshopforexrates.connectors

import play.api.Logging
import play.api.http.HeaderNames.AUTHORIZATION
import play.api.libs.json.Json
import play.api.mvc.ResponseHeader.httpDateFormat
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.onestopshopforexrates.config.IfConfig
import uk.gov.hmrc.onestopshopforexrates.connectors.ExchangeRateHttpParser.*
import uk.gov.hmrc.onestopshopforexrates.model.core.CoreExchangeRateRequest
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import java.net.URL
import java.time.{Clock, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DesConnector @Inject()(
                              httpClientV2: HttpClientV2,
                              ifConfig: IfConfig,
                              clock: Clock
                            )(implicit ec: ExecutionContext) extends Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()

  private[connectors] def headers(correctionId: UUID, date: String): Seq[(String, String)] = ifConfig.ifHeaders(correctionId, date)

  private val url: URL = url"${ifConfig.baseUrl}vec/ecbexchangerate/ecbexchangeraterequest/v1"

  def postLast5DaysToCore(rates: CoreExchangeRateRequest): Future[ExchangeRateResponse] = {
    val correlationId = UUID.randomUUID
    val formattedNow = DateTimeFormatter.RFC_1123_DATE_TIME.format(LocalDateTime.now(clock).atOffset(ZoneOffset.UTC))
    val headersWithCorrelationId = headers(correlationId, formattedNow)

    val headersWithoutAuth = headersWithCorrelationId.filterNot {
      case (key, _) => key.matches(AUTHORIZATION)
    }

    logger.info(s"Sending exchange rate request to core with headers $headersWithoutAuth with body [${Json.toJson(rates)}]")

    httpClientV2.post(url).withBody(Json.toJson(rates)).setHeader(headersWithCorrelationId*).execute[ExchangeRateResponse]
  }

}
