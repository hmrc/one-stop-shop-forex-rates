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

package uk.gov.hmrc.onestopshopforexrates.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{get, ok, urlEqualTo}
import play.api.Application
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate

import java.time.LocalDate

class ForexConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    applicationBuilder
      .configure("microservice.services.forex-rates.port" -> server.port)
      .build()

  private val dateFrom = LocalDate.now().minusDays(5)
  private val dateTo = LocalDate.now()
  val baseCurrency = "EUR"
  val targetCurrency = "GBP"
  val rate = BigDecimal(500)

  val exchangeRate: ExchangeRate = ExchangeRate(
    date = dateFrom,
    baseCurrency = baseCurrency,
    targetCurrency = targetCurrency,
    value = rate
  )

  "getRates" - {

    "must return a list of rates when the backend returns one" in {

      val url = s"/forex-rates/rates/$dateFrom/$dateTo/$baseCurrency/$targetCurrency"

      running(application) {
        val connector = application.injector.instanceOf[ForexConnector]

        val responseBody = Json.toJson(Seq(exchangeRate)).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getRates(dateFrom, dateTo, baseCurrency, targetCurrency).futureValue

        result mustEqual Seq(exchangeRate)
      }
    }
  }

  "getLastRates" - {

    "must return a list of rates when the backend returns" in {

      val numberOfRates = 5

      val url = s"/forex-rates/latest-rates/$numberOfRates/$baseCurrency/$targetCurrency"

      running(application) {
        val connector = application.injector.instanceOf[ForexConnector]

        val responseBody = Json.toJson(Seq(exchangeRate)).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getLastRates(numberOfRates, baseCurrency, targetCurrency).futureValue

        result mustEqual Seq(exchangeRate)
      }
    }
  }
}

