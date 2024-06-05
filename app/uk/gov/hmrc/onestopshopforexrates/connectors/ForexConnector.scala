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

import play.api.{Configuration, Logging}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.onestopshopforexrates.config.Service
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForexConnector @Inject()(
                                httpClientV2: HttpClientV2,
                                config: Configuration
                              )(implicit ec: ExecutionContext) extends Logging {

  private val baseUrl = config.get[Service]("microservice.services.forex-rates")

  def getRates(dateFrom: LocalDate, dateTo: LocalDate, baseCurrency: String, targetCurrency: String): Future[Seq[ExchangeRate]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    httpClientV2.get(url"${baseUrl}rates/$dateFrom/$dateTo/$baseCurrency/$targetCurrency").execute[Seq[ExchangeRate]]
  }

  def getLastRates(numberOfRates: Int, baseCurrency: String, targetCurrency: String): Future[Seq[ExchangeRate]] = {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    httpClientV2.get(url"${baseUrl}latest-rates/$numberOfRates/$baseCurrency/$targetCurrency").execute[Seq[ExchangeRate]]
  }
}
