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