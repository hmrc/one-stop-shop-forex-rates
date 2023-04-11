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

package uk.gov.hmrc.onestopshopforexrates.scheduler.jobs


import akka.actor.ActorSystem
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import uk.gov.hmrc.onestopshopforexrates.scheduler.ScheduledJob
import uk.gov.hmrc.onestopshopforexrates.scheduler.SchedulingActor.RetrieveAndSendExchangeRatesClass
import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesService

import javax.inject.Inject

trait RetrieveAndSendForexDataJob extends ScheduledJob
class RetrieveAndSendForexDataJobImpl @Inject()(val config: Configuration,
                                           val service: ExchangeRatesService,
                                           val applicationLifecycle: ApplicationLifecycle
                                          ) extends RetrieveAndSendForexDataJob {

  val jobName: String           = "RetrieveAndSendForexDataJob"
  val actorSystem: ActorSystem  = ActorSystem(jobName)
  val scheduledMessage: RetrieveAndSendExchangeRatesClass = RetrieveAndSendExchangeRatesClass(service)

  schedule

}
