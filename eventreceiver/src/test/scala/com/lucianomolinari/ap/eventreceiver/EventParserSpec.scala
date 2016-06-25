package com.lucianomolinari.ap.eventreceiver

import com.fasterxml.jackson.core.JsonParseException
import org.scalatest.{Matchers, WordSpec}

class EventParserSpec extends WordSpec with Matchers {
  val parser = new EventParser

  "The parse method" should {

    "return a PageView object when a valid JSON is informed" in {
      val json =
        """
          |{
          |   "userId": 1,
          |   "timestamp": 87387383783,
          |   "page": "/products/123"
          |}
        """.stripMargin
      parser.parse(json) shouldBe PageView(1, 87387383783L, "/products/123")
    }

    "throw an error when an invalid JSON is informed" in {
      an[JsonParseException] should be thrownBy parser.parse("Invalid JSON")
    }

  }

  "The toJson method" should {

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
      parser.toJson(EnrichedPageView(1, "MALE", 87387383783L, "/products/123")) shouldBe expectedJson
    }

  }

}
