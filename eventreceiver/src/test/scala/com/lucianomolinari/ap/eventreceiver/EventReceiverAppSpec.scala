package com.lucianomolinari.ap.eventreceiver

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.TestKit
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class EventReceiverAppSpec extends TestKit(ActorSystem()) with WordSpecLike with Matchers with BeforeAndAfterAll
  with ScalaFutures {

  val logger = LoggerFactory.getLogger(getClass)

  var kafkaProducer: KafkaProducer[String, String] = _
  var kafkaConsumer: KafkaConsumer[String, String] = _

  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(system))

  override implicit def patienceConfig = PatienceConfig(timeout = Span(8, Seconds))

  override def beforeAll {
    EventReceiverApp.configureAndStartConsumers(Array())
    kafkaProducer = KafkaUtil.createKafkaProducer("localhost:9092")
    kafkaConsumer = KafkaUtil.createKafkaConsumer("localhost:9092")
    kafkaConsumer.subscribe(java.util.Arrays.asList(KafkaUtil.pageViewEnrichedTopic))
    addCustomer()
  }

  override def afterAll {
    EventReceiverApp.shutdown
    kafkaProducer.close
    kafkaConsumer.close
    Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
  }

  "Event receiver app" should {

    "Receive an event, process it and send it back to kafka" in {
      val evt = consumePageViewEnrichedEvent

      Thread sleep 1000

      publishAPageViewEvent

      val expectedEnrichedEvent =
        """
          |{
          |   "userId": 1,
          |   "userGender": "FEMALE",
          |   "timestamp": 87387383783,
          |   "page": "/products/123"
          |}
        """.stripMargin.replaceAll(" ", "").split("\n").mkString

      whenReady(evt) {
        resp => resp shouldBe Some(expectedEnrichedEvent)
      }
    }

  }

  def publishAPageViewEvent() = {
    val json =
      """
        |{
        |   "userId": 1,
        |   "timestamp": 87387383783,
        |   "page": "/products/123"
        |}
      """.stripMargin
    logger.info(s"Sending test event $json")
    kafkaProducer.send(new ProducerRecord[String, String](KafkaUtil.pageViewTopic, json))
  }

  def consumePageViewEnrichedEvent(): Future[Option[String]] = Future {
    var result: Option[String] = None
    (1 to 2).toStream.takeWhile(_ => !result.isDefined).foreach(i => {
      logger.info(s"===> Starting pooling $i - $kafkaConsumer")
      val records: ConsumerRecords[String, String] = kafkaConsumer.poll(3000)
      val iterator = records.iterator
      logger.info(s"===> Records returned: ${iterator.hasNext}")
      result = if (iterator.hasNext) Some(iterator.next.value) else None
    })
    result
  }

  def addCustomer() = {
    val entity = HttpEntity(String.format("{\"name\":\"%s\", \"gender\":\"%s\"}", "Mary", "FEMALE"))
      .withContentType(ContentTypes.`application/json`)
    val resp = Http(system)
      .singleRequest(HttpRequest(method = HttpMethods.POST, uri = "http://localhost:8080/customer",
        entity = entity))

    Await.result(resp, 5 second)
  }

}
