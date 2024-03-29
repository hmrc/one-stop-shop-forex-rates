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
import play.api.http.Status.{BAD_REQUEST, CONFLICT}
import play.api.libs.json.Json
import play.api.mvc.ResponseHeader.httpDateFormat
import play.api.test.Helpers.running
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase
import uk.gov.hmrc.onestopshopforexrates.model.core.{CoreErrorResponse, CoreExchangeRateRequest, CoreRate}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDate, LocalDateTime, ZoneId}
import java.util.{Locale, UUID}
import scala.util.Try

class DesConnectorSpec extends SpecBase with WireMockHelper {

  private def application: Application =
    applicationBuilder
      .configure(
        "microservice.services.if.host" -> "127.0.0.1",
        "microservice.services.if.port" -> server.port,
        "microservice.services.if.authorizationToken" -> "auth-token",
        "microservice.services.if.environment" -> "test-environment"
      )
      .build()

  private val baseCurrency = "EUR"
  private val targetCurrency = "GBP"
  private val rate = BigDecimal(500)

  private val exchangeRateRequest: CoreExchangeRateRequest = CoreExchangeRateRequest(
    base = baseCurrency,
    target = targetCurrency,
    timestamp = LocalDateTime.now,
    rates = Seq(CoreRate(LocalDate.now(), rate))
  )

  private val correlationId = UUID.randomUUID()
  private val dateNow = httpDateFormat.format(LocalDateTime.now())

  "headers" - {

    "generate the correct Date header format required" in {
      val dateFormat = DateTimeFormatter
        .ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
        .withZone(ZoneId.of("GMT"))

      val connector = application.injector.instanceOf[DesConnector]

      connector.headers(correlationId, dateNow).filter(item => item._1 == "Date").map { item =>
        Try(dateFormat.parse(item._2)).isSuccess mustBe true
      }
    }

    "generate the correct Correlation ID header required" in {
      val connector = application.injector.instanceOf[DesConnector]
      connector.headers(correlationId, dateNow) must contain("X-Correlation-ID" -> correlationId.toString)
    }

    "generate the correct Accept header required" in {
      val connector = application.injector.instanceOf[DesConnector]
      connector.headers(correlationId, dateNow) must contain("Accept" -> "application/json")
    }

    "generate the correct X-Forwarded-Host header required" in {
      val connector = application.injector.instanceOf[DesConnector]
      connector.headers(correlationId, dateNow) must contain("X-Forwarded-Host" -> "MDTP")
    }

    "generate the correct Content-Type header required" in {
      val connector = application.injector.instanceOf[DesConnector]
      connector.headers(correlationId, dateNow) must contain("Content-Type" -> "application/json")
    }
  }

  "postRates" - {
    val url = "/one-stop-shop-returns-stub/vec/ecbexchangerate/ecbexchangeraterequest/v1"

    "must return Right() when rates are successfully sent to core" in {

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]

        val responseBody = Json.toJson(exchangeRateRequest).toString

        server.stubFor(post(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue

        result mustBe Right((): Unit)
      }
    }

    "must return Left(CoreErrorResponse) when core responds with an error without a response body" in {

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]
        server.stubFor(post(urlEqualTo(url)).willReturn(badRequest()))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue
        val expectedResponse = CoreErrorResponse(result.left.toOption.get.timestamp, None, s"UNEXPECTED_$BAD_REQUEST", "Response body was empty")

        result mustBe Left(expectedResponse)
      }
    }

    "must return Left(CoreErrorResponse) with code 001 when invalid data sent to core" in {

      val uuid = UUID.randomUUID()
      val timestamp = Instant.now(stubClock)
      val errorResponseJson = s"""{
                                |    "timestamp": "$timestamp",
                                |    "transactionId": "$uuid",
                                |    "error": "OSS_001",
                                |    "errorMessage": "Invalid input"
                                |}
                                |""".stripMargin

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(badRequest().withBody(errorResponseJson)))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue

        val expectedResponse = CoreErrorResponse(timestamp, Some(uuid), "OSS_001", "Invalid input")

        result mustBe Left(expectedResponse)
      }
    }

    "must return Left(CoreErrorResponse) with code 409 when data is already present in core" in {

      val errorResponseJson = """{}"""

      running(application) {
        val connector = application.injector.instanceOf[DesConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(status(CONFLICT).withBody(errorResponseJson)))

        val result = connector.postLast5DaysToCore(exchangeRateRequest).futureValue

        val expectedResponse = CoreErrorResponse(result.left.toOption.get.timestamp, result.left.toOption.get.transactionId, s"UNEXPECTED_409", errorResponseJson)

        result mustBe Left(expectedResponse)
      }
    }
  }
}


