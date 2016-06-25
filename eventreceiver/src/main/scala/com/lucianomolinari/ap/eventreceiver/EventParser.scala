package com.lucianomolinari.ap.eventreceiver

import play.api.libs.json.Json

/**
  * Responsible for parsing JSON string into objects and vice-versa.
  */
class EventParser {

  // Needed by play-json
  implicit val pageViewReads = Json.reads[PageView]
  implicit val enrichedPageViewWriters = Json.writes[EnrichedPageView]

  /**
    * Parses a JSON string into a [[PageView]].
    *
    * @param rawJson The JSON representation as String.
    * @return An instance of [[PageView]].
    */
  def parse(rawJson: String): PageView = {
    Json.fromJson[PageView](Json.parse(rawJson)).get
  }

  /**
    * Converts a [[EnrichedPageView]] instance into a JSON representation as String.
    *
    * @param enrichedPageView The [[EnrichedPageView]] instance to be converted
    * @return The JSON representation as String.
    */
  def toJson(enrichedPageView: EnrichedPageView): String = {
    Json.toJson(enrichedPageView).toString
  }

}

case class PageView(userId: Long, timestamp: Long, page: String)

case class EnrichedPageView(userId: Long, userGender: String, timestamp: Long, page: String)
