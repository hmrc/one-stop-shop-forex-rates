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

package uk.gov.hmrc.onestopshopforexrates.config

import play.api.{Configuration, Environment}
import play.api.inject.{Binding, Module}
import uk.gov.hmrc.onestopshopforexrates.scheduler.jobs.{RetrieveAndSendForexDataJob, RetrieveAndSendForexDataJobImpl}
import uk.gov.hmrc.onestopshopforexrates.services.{ExchangeRatesService, ExchangeRatesServiceImpl}

import java.time.{Clock, ZoneOffset}

class ModuleBindings extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[?]] = Seq(
    play.inject.Module.bindClass(classOf[Clock]).toInstance(Clock.systemDefaultZone.withZone(ZoneOffset.UTC)).eagerly().asScala(),
    bind[ExchangeRatesService].to[ExchangeRatesServiceImpl],
    bind[RetrieveAndSendForexDataJob].to[RetrieveAndSendForexDataJobImpl].eagerly()
  )
}
