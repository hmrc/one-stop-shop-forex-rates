package uk.gov.hmrc.onestopshopforexrates.model.core


import play.api.libs.json.*
import uk.gov.hmrc.onestopshopforexrates.base.SpecBase

import java.time.Instant
import java.util.UUID

class CoreErrorResponseSpec extends SpecBase {

  private val randomTimestamp = Instant.now()
  private val uuid = UUID.randomUUID()

  "CoreErrorResponse" - {

    "must deserialise/serialise to and from CoreErrorResponse" in {

      val json = Json.obj(
        "timestamp" -> randomTimestamp,
        "transactionId" -> uuid,
        "error" -> "OSS_001",
        "errorMessage" -> "Invalid input"
      )

      val expectedResult = CoreErrorResponse(
        timestamp = randomTimestamp,
        transactionId = Some(uuid),
        error = "OSS_001",
        errorMessage = "Invalid input"
      )

      Json.toJson(expectedResult) mustEqual json
      json.validate[CoreErrorResponse] mustEqual JsSuccess(expectedResult)
    }

    "must handle optional missing fields during deserialization" in {

      val json = Json.obj(
        "timestamp" -> randomTimestamp,
        "error" -> "OSS_001",
        "errorMessage" -> "Invalid input"
      )

      val expectedResult = CoreErrorResponse(
        timestamp = randomTimestamp,
        transactionId = None,
        error = "OSS_001",
        errorMessage = "Invalid input"
      )

      Json.toJson(expectedResult) mustEqual json
      json.validate[CoreErrorResponse] mustEqual JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {
      val json = Json.obj()

      json.validate[CoreErrorResponse] mustBe a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "timestamp" -> randomTimestamp,
        "transactionId" -> uuid,
        "error" -> 12345,
        "errorMessage" -> "Invalid input"
      )

      json.validate[CoreErrorResponse] mustBe a[JsError]
    }

    "must handle null fields during deserialization" in {

      val json = Json.obj(
        "timestamp" -> randomTimestamp,
        "transactionId" -> uuid,
        "error" -> JsNull,
        "errorMessage" -> "Invalid input"
      )

      json.validate[CoreErrorResponse] mustBe a[JsError]
    }
  }

}


