package uk.gov.hmrc.onestopshopforexrates.repositories

import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.Codecs.toBson
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.onestopshopforexrates.model.ExchangeRate

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ForexRepository @Inject()(
                                 mongoComponent: MongoComponent,
                               )(implicit ec: ExecutionContext)
  extends PlayMongoRepository[ExchangeRate](
    collectionName = "exchangeRates",
    mongoComponent = mongoComponent,
    domainFormat = ExchangeRate.format,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("date", "baseCurrency", "targetCurrency"),
        IndexOptions()
          .name("exchangeRatesIndex")
          .unique(true)
      )
    )
  ) {

  def get(date: LocalDate, baseCurrency: String, targetCurrency: String): Future[Option[ExchangeRate]] = {
    collection
      .find(Filters.and(
        Filters.equal("date", toBson(date)),
        Filters.equal("baseCurrency", toBson(baseCurrency)),
        Filters.equal("targetCurrency", toBson(targetCurrency))
      ))
      .headOption()
  }
}
