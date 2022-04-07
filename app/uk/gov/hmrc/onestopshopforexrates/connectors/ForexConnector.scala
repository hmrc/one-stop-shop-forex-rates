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

import play.api.{Configuration, Logging}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.onestopshopforexrates.config.Service
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForexConnector @Inject()(
                                httpClient: HttpClient,
                                config: Configuration
                              )(implicit ec: ExecutionContext) extends Logging {

  private val baseUrl = config.get[Service]("microservice.services.forex-rates")

  def getRates(dateFrom: LocalDate, dateTo: LocalDate, baseCurrency: String, targetCurrency: String): Future[Seq[ExchangeRate]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    httpClient.GET[Seq[ExchangeRate]](s"${baseUrl}rates/$dateFrom/$dateTo/$baseCurrency/$targetCurrency")
  }

  def getLastRates(numberOfRates: Int, baseCurrency: String, targetCurrency: String): Future[Seq[ExchangeRate]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    httpClient.GET[Seq[ExchangeRate]](s"${baseUrl}rates/latest/$numberOfRates/$baseCurrency/$targetCurrency")
  }
}
