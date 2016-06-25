package com.lucianomolinari.ap.eventreceiver

import com.fasterxml.jackson.core.JsonParseException
import org.scalatest.{Matchers, WordSpec}
import play.api.libs.json.JsResultException

class JsonConverterSpec extends WordSpec with Matchers {
  val jsonConverter = new JsonConverter

  "The fromJsonToPageView method" should {

    "return a PageView object when a valid JSON is informed" in {
      val json =
        """
          |{
          |   "userId": 1,
          |   "timestamp": 87387383783,
          |   "page": "/products/123"
          |}
        """.stripMargin
      jsonConverter.fromJsonToPageView(json) shouldBe PageView(1, 87387383783L, "/products/123")
    }

    "throw an error when an invalid JSON is informed" in {
      an[JsonParseException] should be thrownBy jsonConverter.fromJsonToPageView("Invalid JSON")
    }

  }

  "The fromEnrichedPageViewToJson method" should {

    "return the JSON formatted as string when an EnrichedPageView is informed" in {
      val expectedJson =
        """
          |{
          |   "userId": 1,
          |   "userGender": "MALE",
          |   "timestamp": 87387383783,
          |   "page": "/products/123"
          |}
        """.stripMargin.replaceAll(" ", "").split("\n").mkString
      jsonConverter.fromEnrichedPageViewToJson(EnrichedPageView(1, "MALE", 87387383783L, "/products/123")) shouldBe expectedJson
    }

  }

  "The extractUserGenderFromJson method" should {

    "return the gender of the user when a valid JSON is informed" in {
      val json =
        """
          |{
          |   "id": 1,
          |   "name": "John",
          |   "gender": "MALE"
          |}
        """.stripMargin
      jsonConverter.extractUserGenderFromJson(json) shouldBe "MALE"
    }

    "throw exception if JSON informed doesn't have gender field" in {
      val json =
        """
          |{
          |   "id": 1,
          |   "name": "John"
          |}
        """.stripMargin
      an[JsResultException] should be thrownBy jsonConverter.extractUserGenderFromJson(json)
    }

  }

}
