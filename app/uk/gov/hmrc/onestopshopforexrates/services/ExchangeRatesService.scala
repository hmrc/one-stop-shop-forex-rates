/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.onestopshopforexrates.services

import uk.gov.hmrc.onestopshopforexrates.config.AppConfig
import uk.gov.hmrc.onestopshopforexrates.connectors.ExchangeRateHttpParser.ExchangeRateResponse
import uk.gov.hmrc.onestopshopforexrates.connectors.{DesConnector, ForexConnector}
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate
import uk.gov.hmrc.onestopshopforexrates.model.core.{CoreExchangeRateRequest, CoreRate}

import java.time.{Clock, LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ExchangeRatesService @Inject()(forexConnector: ForexConnector,
                                     desConnector: DesConnector,
                                     clock: Clock,
                                     appConfig: AppConfig
                                    )(implicit ec: ExecutionContext) {

  private val dateTo = LocalDate.now(clock)
  private val dateFrom = dateTo.minusDays(4)
  private val baseCurrency = "EUR"
  private val targetCurrency = "GBP"
  private val timestamp = LocalDateTime.now(clock)

  def retrieveAndSendToCore(): Future[ExchangeRateResponse] = {
    val retrievedExchangeRateData = forexConnector.getRates(dateFrom, dateTo, baseCurrency, targetCurrency)
    retrievedExchangeRateData.flatMap {
      exchangeRates => {
        val exchangeRateRequest = exchangeRateToExchangeRateRequest(exchangeRates)
        retrySendingRates(appConfig.desConnectorMaxAttempts, exchangeRateRequest)
      }
    }
  }

  private def exchangeRateToExchangeRateRequest(exchangeRates: Seq[ExchangeRate]): CoreExchangeRateRequest = {
    val coreRates = exchangeRates.map(
      exchangeRate =>
        CoreRate(exchangeRate.date, exchangeRate.value)
    )
    CoreExchangeRateRequest(baseCurrency, targetCurrency, timestamp, coreRates)
  }

  private def retrySendingRates(count: Int, coreRequest: CoreExchangeRateRequest): Future[ExchangeRateResponse] = {
    desConnector.postLast5DaysToCore(coreRequest).flatMap {
      response =>
        response match {
          case Right(_) =>
            Future.successful(response)
          case Left(_) =>
            if (count > 1) retrySendingRates(count - 1, coreRequest)
            else Future.successful(response)
        }
    }
  }
}

