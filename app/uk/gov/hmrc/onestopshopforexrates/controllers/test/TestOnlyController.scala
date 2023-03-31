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

package uk.gov.hmrc.onestopshopforexrates.controllers.test

import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesServiceImpl
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TestOnlyController @Inject()(
                                    cc: ControllerComponents,
                                    scheduledService: ExchangeRatesServiceImpl
                                  )(implicit ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  def retrieveRates(): Action[AnyContent] = Action.async {
    for {
      result <- scheduledService.invoke
    } yield {
      if(result) {
        logger.info("Successfully retrieved rates from forex-rates and sent to Core")
        Ok
      } else {
        logger.warn("Did not complete RetrieveAndSendForexDataJob")
        NotFound
      }


    }
  }

}