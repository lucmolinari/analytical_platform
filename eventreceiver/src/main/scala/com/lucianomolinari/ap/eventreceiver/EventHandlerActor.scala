package com.lucianomolinari.ap.eventreceiver

import akka.actor.{Actor, ActorLogging}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

/**
  * Actor responsible for handling the incoming events by parsing, enriching and then sending them to Kafka.
  *
  * @param parser   The [[EventParser]] used to deal with JSON.
  * @param producer The [[KafkaProducer]] used to send the enriched events.
  */
class EventHandlerActor(val parser: EventParser, val producer: KafkaProducer[String, String]) extends Actor
  with ActorLogging {

  def receive = {
    case record: String => {
      log.debug(s"Raw record: ${record}")
      val pageView = parser.parse(record)

      //TODO: Proper event enrichment
      val enrichedEvt = EnrichedPageView(pageView.userId, "MALE", pageView.timestamp, pageView.page)

      val jsonOutput = parser.toJson(enrichedEvt)
      producer.send(new ProducerRecord[String, String](KafkaUtil.pageViewEnrichedTopic, jsonOutput))
      log.debug(s"Event sent: $jsonOutput")
    }
    case _ => log.warning("Received unrecognized message")
  }

}
