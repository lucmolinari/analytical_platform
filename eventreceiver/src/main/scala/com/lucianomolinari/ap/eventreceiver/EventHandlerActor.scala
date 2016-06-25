package com.lucianomolinari.ap.eventreceiver

import akka.actor.{Actor, ActorLogging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.ByteString
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.concurrent._
import scala.concurrent.duration._

/**
  * Actor responsible for handling the incoming events by parsing, enriching and then sending them to Kafka.
  * <p>
  * This actor blocks while waiting for HTTP responses, which might be an issue. However, it runs on its
  * own router and dispatcher.
  * <p>
  * If something goes wrong during the event processing, the code just let it crash. Alternatively, these events
  * could be sent to some dead letter topic.
  *
  * @param customerAppHost Host where the customer app can be reached. It's informed in the format host:port
  * @param producer        The [[KafkaProducer]] used to send the enriched events.
  */
class EventHandlerActor(customerAppHost: String, producer: KafkaProducer[String, String]) extends Actor
  with ActorLogging {

  val jsonConverter = new JsonConverter

  val Array(host, port) = customerAppHost.split(":")
  val connectionFlow: Flow[HttpRequest, HttpResponse, Future[Http.OutgoingConnection]] =
    Http(context.system).outgoingConnection(host = host, port = port.toInt)
  implicit val materializer = ActorMaterializer()

  def receive = {
    case record: String => {
      log.debug(s"Raw record: ${record}")
      val pageView = jsonConverter.fromJsonToPageView(record)

      val optGender = findCustomerGender(pageView.userId)
      if (optGender.isDefined) {
        val enrichedEvt = EnrichedPageView(pageView.userId, optGender.get, pageView.timestamp, pageView.page)
        sendEnrichedEvent(enrichedEvt)
      }
    }
    case _ => log.warning("Received unrecognized message")
  }

  def findCustomerGender(userId: Long): Option[String] = {
    val futureResponse: Future[HttpResponse] = Source.single(HttpRequest(uri = s"/customer/$userId"))
      .via(connectionFlow).runWith(Sink.head)

    val resp: HttpResponse = Await.result(futureResponse, 3 seconds)
    resp match {
      case HttpResponse(StatusCodes.OK, headers, entity, _) =>
        val futureBody: Future[ByteString] = entity.dataBytes.runFold(ByteString(""))(_ ++ _)
        val body = Await.result(futureBody, 3 seconds).utf8String
        log.debug(s"Response received : ${body}")
        Some(jsonConverter.extractUserGenderFromJson(body))

      case HttpResponse(code, _, _, _) =>
        log.error(s"Couldn't find customer $userId for some reason. Return HTTP Code $code")
        None
      case unexpected =>
        log.error(s"Got unexpected message while finding customer $userId: $unexpected")
        None
    }
  }

  def sendEnrichedEvent(event: EnrichedPageView) = {
    val jsonOutput = jsonConverter.fromEnrichedPageViewToJson(event)
    producer.send(new ProducerRecord[String, String](KafkaUtil.pageViewEnrichedTopic, jsonOutput))
    log.debug(s"Event sent: $jsonOutput")
  }

}