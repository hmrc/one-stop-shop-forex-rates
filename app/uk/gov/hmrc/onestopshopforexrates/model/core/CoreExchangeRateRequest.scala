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

package uk.gov.hmrc.onestopshopforexrates.model.core

import play.api.libs.json.{OFormat, OWrites, Reads, __}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDateTime

case class CoreExchangeRateRequest(base: String, target: String, timestamp: LocalDateTime, rates: Seq[CoreRate])

object CoreExchangeRateRequest {

  val reads: Reads[CoreExchangeRateRequest] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "base").read[String] and
        (__ \ "target").read[String] and
        (__ \ "timestamp").read(MongoJavatimeFormats.localDateTimeReads) and
        (__ \ "rates").read[Seq[CoreRate]]
      ) (CoreExchangeRateRequest.apply _)
  }

  val writes: OWrites[CoreExchangeRateRequest] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "base").write[String] and
        (__ \ "target").write[String] and
        (__ \ "timestamp").write(MongoJavatimeFormats.localDateTimeFormat) and
        (__ \ "rates").write[Seq[CoreRate]]
      ) (unlift(CoreExchangeRateRequest.unapply))
  }

  implicit val format: OFormat[CoreExchangeRateRequest] = OFormat(reads, writes)

}