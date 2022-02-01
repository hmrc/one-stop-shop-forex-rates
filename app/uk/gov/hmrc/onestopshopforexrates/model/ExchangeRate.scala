package uk.gov.hmrc.onestopshopforexrates.model

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDateTime

case class ExchangeRate(date: LocalDateTime, baseCurrency: String, targetCurrency: String, value: BigDecimal)

object ExchangeRate {
  implicit val format: OFormat[ExchangeRate] = Json.format[ExchangeRate]
}
