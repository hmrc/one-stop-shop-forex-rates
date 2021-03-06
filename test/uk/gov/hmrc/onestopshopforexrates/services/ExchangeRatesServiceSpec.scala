package uk.gov.hmrc.onestopshopforexrates.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase
import uk.gov.hmrc.onestopshopforexrates.config.AppConfig
import uk.gov.hmrc.onestopshopforexrates.connectors.{DesConnector, ForexConnector}
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate
import uk.gov.hmrc.onestopshopforexrates.model.core.{CoreErrorResponse, CoreExchangeRateRequest}

import java.time.{Instant, LocalDate}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ExchangeRatesServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockForexConnector = mock[ForexConnector]
  private val mockDesConnector = mock[DesConnector]
  private val mockAppConfig = mock[AppConfig]

  private val date = LocalDate.now(stubClock)
  private val baseCurrency = "EUR"
  private val targetCurrency = "GBP"
  private val rate = BigDecimal(100)
  private val timestamp = Instant.now(stubClock)

  private val maxAttempts = 3

  private val exchangeRate = ExchangeRate(date, baseCurrency, targetCurrency, rate)

  override def beforeEach(): Unit = {
    Mockito.reset(mockForexConnector)
    Mockito.reset(mockDesConnector)
    Mockito.reset(mockAppConfig)
  }

  "if getByLatestRates is disabled" - {

    val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

    "must retrieve exchange rate data from Forex Rates and send to Core" in {
      when(mockAppConfig.desConnectorMaxAttempts) thenReturn maxAttempts
      when(mockForexConnector.getRates(any(), any(), any(), any())) thenReturn Future.successful(Seq(exchangeRate))
      when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.successful(Right((): Unit))

      val result = exchangeRateService.retrieveAndSendToCore().futureValue

      result mustBe Right((): Unit)

      verify(mockForexConnector, times(1)).getRates(any[LocalDate], any[LocalDate], any[String], any[String])
      verify(mockDesConnector, times(1)).postLast5DaysToCore(any[CoreExchangeRateRequest])
    }

    "must not send to Core when there are no Forex exchange rates retrieved" in {
      when(mockAppConfig.desConnectorMaxAttempts) thenReturn maxAttempts
      when(mockForexConnector.getRates(any(), any(), any(), any())) thenReturn Future.successful(Seq.empty)

      val result = exchangeRateService.retrieveAndSendToCore().futureValue

      result mustBe Right((): Unit)

      verify(mockForexConnector, times(1)).getRates(any[LocalDate], any[LocalDate], any[String], any[String])
      verifyNoInteractions(mockDesConnector)
    }

    "must retry sending exchange rate data to Core up to a maximum of 3 tries" in {
      val expectedResponse = CoreErrorResponse(timestamp, Some(UUID.randomUUID()), s"UNEXPECTED_400", "errorResponseJson")

      when(mockAppConfig.desConnectorMaxAttempts) thenReturn maxAttempts
      when(mockForexConnector.getRates(any(), any(), any(), any())) thenReturn Future.successful(Seq(exchangeRate))
      when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.successful(Left(expectedResponse))

      val result = exchangeRateService.retrieveAndSendToCore().futureValue

      result mustBe Left(expectedResponse)

      verify(mockForexConnector, times(1)).getRates(any[LocalDate], any[LocalDate], any[String], any[String])
      verify(mockDesConnector, times(maxAttempts)).postLast5DaysToCore(any())
    }
  }

  "if getByLatestRates is enabled" - {

    when(mockAppConfig.getByLatestRates) thenReturn true
    when(mockAppConfig.desConnectorMaxAttempts) thenReturn maxAttempts
    val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

    "must retrieve exchange rate data from Forex Rates and send to Core" in {
      when(mockForexConnector.getLastRates(any(), any(), any())) thenReturn Future.successful(Seq(exchangeRate))
      when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.successful(Right((): Unit))

      val result = exchangeRateService.retrieveAndSendToCore().futureValue

      result mustBe Right((): Unit)

      verify(mockForexConnector, times(1)).getLastRates(any[Int], any[String], any[String])
      verify(mockDesConnector, times(1)).postLast5DaysToCore(any[CoreExchangeRateRequest])
    }
  }
}


