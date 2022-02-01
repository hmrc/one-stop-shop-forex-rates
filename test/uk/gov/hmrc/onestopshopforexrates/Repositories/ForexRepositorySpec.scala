package uk.gov.hmrc.onestopshopforexrates.Repositories

import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.mongo.test.{CleanMongoCollectionSupport, DefaultPlayMongoRepositorySupport}
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate
import uk.gov.hmrc.onestopshopforexrates.repositories.ForexRepository

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ForexRepositorySpec
extends AnyFreeSpec
with Matchers
with DefaultPlayMongoRepositorySupport[ExchangeRate]
with CleanMongoCollectionSupport
with ScalaFutures
with IntegrationPatience
with OptionValues {

  override protected val repository =
    new ForexRepository(
      mongoComponent = mongoComponent
    )
  ".get" - {

    "must return none when no data exists for requested date" in {

      val requestDate = LocalDate.now
      val baseCurrency = "GBP"
      val targetCurrency = "DUP"

      val result = repository.get(requestDate, baseCurrency, targetCurrency).futureValue

      result mustBe None
    }

    "must return a valid ExchangeRate when data exists for requested date" in {

      val requestDate = LocalDate.now
      val baseCurrency = "GBP"
      val targetCurrency = "DUP"

      insert(ExchangeRate(requestDate.atStartOfDay(), baseCurrency, targetCurrency, BigDecimal(10))).futureValue

      val result = repository.get(requestDate, baseCurrency, targetCurrency).futureValue

      result mustBe ExchangeRate(requestDate.atStartOfDay(), baseCurrency, targetCurrency, BigDecimal(10))
    }


  }

}
