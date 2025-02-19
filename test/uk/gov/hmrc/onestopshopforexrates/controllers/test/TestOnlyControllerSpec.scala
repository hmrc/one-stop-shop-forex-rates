package uk.gov.hmrc.onestopshopforexrates.controllers.test

import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase
import uk.gov.hmrc.onestopshopforexrates.services.ExchangeRatesServiceImpl

import scala.concurrent.{ExecutionContext, Future}

class TestOnlyControllerSpec extends SpecBase with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  "TestOnlyController" - {

    "return OK when retrieveRates succeeds" in {
      val mockService = mock[ExchangeRatesServiceImpl]
      when(mockService.invoke).thenReturn(Future.successful(true))

      val controller = new TestOnlyController(Helpers.stubControllerComponents(), mockService)
      val result: Future[Result] = controller.retrieveRates()(FakeRequest())

      status(result) mustBe OK
      contentAsString(result) mustBe ""
    }

    "return NotFound when retrieveRates fails" in {
      val mockService = mock[ExchangeRatesServiceImpl]
      when(mockService.invoke).thenReturn(Future.successful(false))

      val controller = new TestOnlyController(Helpers.stubControllerComponents(), mockService)
      val result: Future[Result] = controller.retrieveRates()(FakeRequest())

      status(result) mustBe NOT_FOUND
    }
  }
}


