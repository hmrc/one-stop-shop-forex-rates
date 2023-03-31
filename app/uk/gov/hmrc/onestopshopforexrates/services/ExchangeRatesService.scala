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

package uk.gov.hmrc.onestopshopforexrates.services

import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.onestopshopforexrates.config.AppConfig
import uk.gov.hmrc.onestopshopforexrates.connectors.ExchangeRateHttpParser.ExchangeRateResponse
import uk.gov.hmrc.onestopshopforexrates.connectors.{DesConnector, ForexConnector}
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate
import uk.gov.hmrc.onestopshopforexrates.model.core.{CoreExchangeRateRequest, CoreRate}
import uk.gov.hmrc.onestopshopforexrates.scheduler.ScheduledService

import java.time.{Clock, LocalDate, LocalDateTime}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

trait ExchangeRatesService extends ScheduledService[Boolean] with Logging
class ExchangeRatesServiceImpl @Inject()(forexConnector: ForexConnector,
                                     desConnector: DesConnector,
                                     clock: Clock,
                                     appConfig: AppConfig
                                    )(implicit ec: ExecutionContext) extends ExchangeRatesService {

  private val dateTo = LocalDate.now(clock)
  private val dateFrom = dateTo.minusDays(4)
  private val baseCurrency = appConfig.baseCurrency
  private val targetCurrency = appConfig.targetCurrency
  private val numberOfRates = appConfig.numberOfRates
  private val getByLatestRates = appConfig.getByLatestRates
  private val timestamp = LocalDateTime.now(clock)
  override val jobName: String = "RetrieveAndSendForexDataJob"

  override def invoke(implicit ec: ExecutionContext): Future[Boolean] = {
    logger.info(s"[$jobName Scheduled Job Started]")

    retrieveAndSendToCore().map {
      case Right(_) =>
        logger.info(s"[$jobName ran successfully]")
        true
      case Left(error) =>
        logger.error(s"[Error when running $jobName: ${error.errorMessage}]")
        false
    }.recover {
      case e: Exception =>
        logger.error(s"[$jobName] Error occurred during retrieve and send to core: ${e.getMessage}", e)
        throw e
    }
  }

  def retrieveAndSendToCore(): Future[ExchangeRateResponse] = {
    val retrievedExchangeRateData = {
      if(getByLatestRates) {
        forexConnector.getLastRates(numberOfRates, baseCurrency, targetCurrency)
      } else {
        forexConnector.getRates(dateFrom, dateTo, baseCurrency, targetCurrency)
      }
    }
    retrievedExchangeRateData.flatMap {
      exchangeRates => if(exchangeRates.isEmpty) {
        logger.warn("There were no rates retrieved")
        Future.successful(Right((): Unit))
      } else {
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
      case response@Right(_) =>
        Future.successful(response)
      case response@Left(_) =>
        logger.error(s"Received failure when sending exchange rate to core status: [${response.value}]. Request was: [${Json.toJson(coreRequest)}]")
        if (count > 1) retrySendingRates(count - 1, coreRequest)
        else Future.successful(response)
    }.recoverWith {
      case e: Exception =>
        logger.error(s"General error occurred ${e.getMessage}", e)
        if (count > 1) retrySendingRates(count - 1, coreRequest)
        else throw e
    }
  }
}

