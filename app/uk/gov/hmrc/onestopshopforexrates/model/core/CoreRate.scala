package uk.gov.hmrc.onestopshopforexrates.model.core

import play.api.libs.json.{OFormat, OWrites, Reads, __}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.LocalDate

case class CoreRate(publishedDate: LocalDate, rate: BigDecimal)

object CoreRate {

   val reads: Reads[CoreRate] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "publishedDate").read(MongoJavatimeFormats.localDateFormat) and
        (__ \ "rate").read[BigDecimal]
      ) (CoreRate.apply _)
  }

  val writes: OWrites[CoreRate] = {

    import play.api.libs.functional.syntax._

    (
      (__ \ "publishedDate").write(MongoJavatimeFormats.localDateFormat) and
        (__ \ "rate").write[BigDecimal]
      ) (unlift(CoreRate.unapply))
  }

  implicit val format: OFormat[CoreRate] = OFormat(reads, writes)

}