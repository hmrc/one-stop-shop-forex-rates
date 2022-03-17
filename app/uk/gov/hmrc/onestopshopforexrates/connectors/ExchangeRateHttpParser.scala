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

import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.onestopshopforexrates.model.core.CoreErrorResponse

import java.time.Instant

object ExchangeRateHttpParser extends Logging {

  type ExchangeRateResponse = Either[CoreErrorResponse, Unit]

  implicit object ExchangeRateReads extends HttpReads[ExchangeRateResponse] {
    override def read(method: String, url: String, response: HttpResponse): ExchangeRateResponse =
      response.status match {
        case OK =>
          Right()
        case status =>
          if(response.body.isEmpty){
            logger.error(s"Recieved status code back $status with empty response body")
            Left(CoreErrorResponse(Instant.now(), None, s"UNEXPECTED_$status", "Response body was empty"))
          } else {
            logger.error(s"Recieved status code back $status with body [${response.body}]")
            response.json.validateOpt[CoreErrorResponse] match {
              case JsSuccess(Some(value), _) =>
                logger.error(s"Error response from core $url, received status $status, body of response was: ${response.body}")
                Left(value)
              case _ =>
                logger.error(s"Unexpected error response from core $url, received status $status, body of response was: ${response.body}")
                Left(CoreErrorResponse(Instant.now(), None, s"UNEXPECTED_$status", response.body))
            }
          }
      }
  }

}
