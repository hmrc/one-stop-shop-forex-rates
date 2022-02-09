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

package uk.gov.hmrc.onestopshopforexrates.model

import play.api.libs.json.{OFormat, OWrites, Reads, __}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.{LocalDate, LocalDateTime}

case class ExchangeRate(date: LocalDate, baseCurrency: String, targetCurrency: String, value: BigDecimal)

object ExchangeRate {

  val reads: Reads[ExchangeRate] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "date").read(MongoJavatimeFormats.localDateFormat) and
        (__ \ "baseCurrency").read[String] and
        (__ \ "targetCurrency").read[String] and
        (__ \ "value").read[BigDecimal]
      ) (ExchangeRate.apply _)
  }

  val writes: OWrites[ExchangeRate] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "date").write(MongoJavatimeFormats.localDateFormat) and
        (__ \ "baseCurrency").write[String] and
        (__ \ "targetCurrency").write[String] and
        (__ \ "value").write[BigDecimal]
      ) (unlift(ExchangeRate.unapply))
  }

  implicit val format: OFormat[ExchangeRate] = OFormat(reads, writes)

}