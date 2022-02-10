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

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.Application
import play.api.http.Status.CONFLICT
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase
import uk.gov.hmrc.onestopshopforexrates.model.core.{CoreErrorResponse, CoreExchangeRateRequest, CoreRate}

import java.time.{LocalDate, LocalDateTime}

class DesConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    applicationBuilder
      .configure(
        "microservice.services.des.host" -> "127.0.0.1",
        "microservice.services.des.port" -> server.port,
        "microservice.services.des.authorizationToken" -> "auth-token",
        "microservice.services.des.environment" -> "test-environment"
      )
      .build()

  val baseCurrency = "EUR"
  val targetCurrency = "GBP"
  val rate = BigDecimal(500)

  val exchangeRateRequest: CoreExchangeRateRequest = CoreExchangeRateRequest(
    base = baseCurrency,
    target = targetCurrency,
    timestamp = LocalDateTime.now,
    rates = Seq(CoreRate(LocalDate.now, rate))
  )

  "postRates" - {

    "must return OK when rates are successfully sent to core" in {

      val url = "/one-stop-shop-returns-stub/oss/referencedata/v1/exchangerate"

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]

        val responseBody = Json.toJson(exchangeRateRequest).toString

        server.stubFor(post(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue

        result mustBe Right()
      }
    }

    "must return BadRequest when invalid data sent to core" in {

      val url = "/one-stop-shop-returns-stub/oss/referencedata/v1/exchangerate"

      val errorResponseJson = """{}"""

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(badRequest().withBody(errorResponseJson)))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue

        val expectedResponse = CoreErrorResponse(result.left.get.timestamp, result.left.get.transactionId, s"UNEXPECTED_400", errorResponseJson)

        result mustBe Left(expectedResponse)
      }
    }

    "must return Conflict Found when data is already present in core" in {

      val url = "/one-stop-shop-returns-stub/oss/referencedata/v1/exchangerate"

      val errorResponseJson = """{}"""

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(status(CONFLICT).withBody(errorResponseJson)))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue

        val expectedResponse = CoreErrorResponse(result.left.get.timestamp, result.left.get.transactionId, s"UNEXPECTED_409", errorResponseJson)

        result mustBe Left(expectedResponse)
      }
    }
  }
}

