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

package uk.gov.hmrc.onestopshopforexrates.config

import play.api.Configuration
import play.api.mvc.ResponseHeader.httpDateFormat

import java.time.{Clock, LocalDateTime, ZonedDateTime}
import java.util.UUID
import javax.inject.Inject

class IfConfig @Inject()(config: Configuration, clock: Clock) {

  val baseUrl: Service = config.get[Service]("microservice.services.if")
  val authorizationToken: String = config.get[String]("microservice.services.if.authorizationToken")
  val environment: String = config.get[String]("microservice.services.if.environment")

  def ifHeaders(correlationId: UUID): Seq[(String, String)] = Seq(
    "Authorization" -> s"Bearer $authorizationToken",
    "Date" -> httpDateFormat.format(LocalDateTime.now(clock)),
    "X-Correlation-ID" -> correlationId.toString,
    "Accept" -> "application/json",
    "X-Forwarded-Host" -> "MDTP",
    "Content-Type" -> "application/json"
  )
}