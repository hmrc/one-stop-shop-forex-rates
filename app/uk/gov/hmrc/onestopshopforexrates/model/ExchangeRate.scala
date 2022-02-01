package uk.gov.hmrc.onestopshopforexrates.model

import java.time.LocalDateTime

case class ExchangeRate(date: LocalDateTime, baseCurrency: String, targetCurrency: String, value: BigDecimal)
