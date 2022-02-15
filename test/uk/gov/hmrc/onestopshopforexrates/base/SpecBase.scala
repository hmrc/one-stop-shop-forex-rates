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

package uk.gov.hmrc.onestopshopforexrates.base

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import uk.gov.hmrc.onestopshopforexrates.scheduler.jobs.RetrieveAndSendForexDataJob
import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesService

import java.time.{Clock, Instant, ZoneId}


trait SpecBase extends AnyFreeSpec
  with Matchers
  with ScalaFutures
  with IntegrationPatience
  with ScalaCheckPropertyChecks
  with MockitoSugar {

  val now = Instant.now
  val stubClock = Clock.fixed(now, ZoneId.systemDefault())

  protected def applicationBuilder: GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[ExchangeRatesService].to[FakeExchangeRateService],
        bind[RetrieveAndSendForexDataJob].to[FakeRetrieveAndSendForexDataJob]
      )
}