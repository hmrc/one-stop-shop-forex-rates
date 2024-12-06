package uk.gov.hmrc.onestopshopforexrates.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase
import uk.gov.hmrc.onestopshopforexrates.config.AppConfig
import uk.gov.hmrc.onestopshopforexrates.connectors.{DesConnector, ForexConnector}
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate
import uk.gov.hmrc.onestopshopforexrates.model.core.{CoreErrorResponse, CoreExchangeRateRequest, CoreRate}

import java.time.{Clock, Instant, LocalDate, LocalDateTime, ZoneId}
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

  "invoke" - {

    "must log job start and success messages and return true when the job runs successfully" in {
      val mockForexConnector = mock[ForexConnector]
      val mockDesConnector = mock[DesConnector]
      val mockAppConfig = mock[AppConfig]
      val stubClock = Clock.fixed(Instant.now, ZoneId.systemDefault())

      val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

      when(mockAppConfig.getByLatestRates) thenReturn false
      when(mockAppConfig.desConnectorMaxAttempts) thenReturn 3
      when(mockForexConnector.getRates(any(), any(), any(), any())) thenReturn Future.successful(Seq(exchangeRate))
      when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.successful(Right((): Unit))

      val result = exchangeRateService.invoke.futureValue

      result mustBe true

      verify(mockForexConnector, times(1)).getRates(any[LocalDate], any[LocalDate], any[String], any[String])
      verify(mockDesConnector, times(1)).postLast5DaysToCore(any[CoreExchangeRateRequest])
    }
  }

  "must log job start and failure messages and return false when the job fails" in {
    val mockForexConnector = mock[ForexConnector]
    val mockDesConnector = mock[DesConnector]
    val mockAppConfig = mock[AppConfig]
    val stubClock = Clock.fixed(Instant.now, ZoneId.systemDefault())

    val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

    val expectedResponse = CoreErrorResponse(timestamp, Some(UUID.randomUUID()), s"UNEXPECTED_400", "errorResponseJson")

    when(mockAppConfig.getByLatestRates) thenReturn false
    when(mockAppConfig.desConnectorMaxAttempts) thenReturn 3
    when(mockForexConnector.getRates(any(), any(), any(), any())) thenReturn Future.successful(Seq(exchangeRate))
    when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.successful(Left(expectedResponse))

    val result = exchangeRateService.invoke.futureValue

    result mustBe false

    verify(mockForexConnector, times(1)).getRates(any[LocalDate], any[LocalDate], any[String], any[String])
    verify(mockDesConnector, times(3)).postLast5DaysToCore(any[CoreExchangeRateRequest])
  }

  "must log error when an exception occurs during invoke" in {
    val mockForexConnector = mock[ForexConnector]
    val mockDesConnector = mock[DesConnector]
    val mockAppConfig = mock[AppConfig]
    val stubClock = Clock.fixed(Instant.now, ZoneId.systemDefault())

    val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

    when(mockAppConfig.getByLatestRates) thenReturn false
    when(mockForexConnector.getRates(any(), any(), any(), any())) thenReturn Future.failed(new RuntimeException("Unexpected error"))

    val futureResult = exchangeRateService.invoke.failed

    whenReady(futureResult) { exception =>
      exception mustBe a[RuntimeException]
      exception.getMessage mustBe "Unexpected error"
    }

    verify(mockForexConnector, times(1)).getRates(any[LocalDate], any[LocalDate], any[String], any[String])
    verifyNoInteractions(mockDesConnector)
  }

  "retrySendingRates" - {

    "must log an error when retrying and eventually fail after max attempts" in {
      val mockForexConnector = mock[ForexConnector]
      val mockDesConnector = mock[DesConnector]
      val mockAppConfig = mock[AppConfig]
      val stubClock = Clock.fixed(Instant.now, ZoneId.systemDefault())

      val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

      val expectedResponse = CoreErrorResponse(timestamp, Some(UUID.randomUUID()), s"UNEXPECTED_400", "errorResponseJson")

      when(mockAppConfig.desConnectorMaxAttempts) thenReturn 3
      when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.successful(Left(expectedResponse))

      val exchangeRateRequest = CoreExchangeRateRequest(
        base = baseCurrency,
        target = targetCurrency,
        timestamp = LocalDateTime.now,
        rates = Seq(CoreRate(LocalDate.now(), rate))
      )

      val result = exchangeRateService.retrySendingRates(3, exchangeRateRequest).futureValue

      result mustBe Left(expectedResponse)

      verify(mockDesConnector, times(3)).postLast5DaysToCore(any[CoreExchangeRateRequest])
    }

    "must throw an exception after retries fail and log the error" in {
      val mockForexConnector = mock[ForexConnector]
      val mockDesConnector = mock[DesConnector]
      val mockAppConfig = mock[AppConfig]
      val stubClock = Clock.fixed(Instant.now, ZoneId.systemDefault())

      val exchangeRateService = new ExchangeRatesServiceImpl(mockForexConnector, mockDesConnector, stubClock, mockAppConfig)

      when(mockAppConfig.desConnectorMaxAttempts) thenReturn 3
      when(mockDesConnector.postLast5DaysToCore(any())) thenReturn Future.failed(new RuntimeException("Network issue"))

      val exchangeRateRequest = CoreExchangeRateRequest(
        base = baseCurrency,
        target = targetCurrency,
        timestamp = LocalDateTime.now(stubClock),
        rates = Seq(CoreRate(LocalDate.now(), rate))
      )

      val futureResult = exchangeRateService.retrySendingRates(3, exchangeRateRequest).failed

      whenReady(futureResult) { exception =>
        exception mustBe a[RuntimeException]
        exception.getMessage mustBe "Network issue"
      }

      verify(mockDesConnector, times(3)).postLast5DaysToCore(any[CoreExchangeRateRequest])
    }
  }

}


