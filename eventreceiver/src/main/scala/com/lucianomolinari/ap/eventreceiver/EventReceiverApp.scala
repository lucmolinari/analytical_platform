package com.lucianomolinari.ap.eventreceiver

import java.util.concurrent._

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.routing.RoundRobinPool
import org.apache.kafka.clients.producer.KafkaProducer
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global

object EventReceiverApp {

  val help =
    """Arguments needed to execute event receiver:
      | Kafka bootstrap servers. Default value: localhost:9092
      | Customer Application server. Default value: localhost:8080
      | Number of consumers. Default value: 1
    """.stripMargin

  val logger = LoggerFactory.getLogger(getClass)

  implicit val system = ActorSystem("Actor-Receiver")

  private var consumers: List[EventReceiver] = _
  private var kafkaProducer: KafkaProducer[String, String] = _
  private var executor: ExecutorService = _

  def main(args: Array[String]): Unit = {
    if (args.length == 1 && args(0).equals("--h")) {
      println(help)
      return
    }

    this.configureAndStartConsumers(args)

    sys addShutdownHook {
      logger.info(s"===> Stopping all consumers and producer")
      shutdown
    }
  }

  def shutdown() = {
    consumers.foreach(_.shutdown)
    executor.shutdown
    kafkaProducer.close
    Http().shutdownAllConnectionPools() andThen { case _ => system.terminate() }
  }

  def configureAndStartConsumers(args: Array[String]) = {
    // TODO: Use proper service discovery
    val kafkaBootstrapServers = args.lift(0).getOrElse("localhost:9092")
    val customerApp = args.lift(1).getOrElse("localhost:8080")
    val numberOfConsumers = args.lift(2).getOrElse("1").toInt

    logger.info(s"Running $numberOfConsumers Kafka consumer connecting to kafka servers ($kafkaBootstrapServers) " +
      s"and customer app server ($customerApp)")

    this.kafkaProducer = KafkaUtil.createKafkaProducer(kafkaBootstrapServers)

    val workers = system.actorOf(Props(classOf[EventHandlerActor], customerApp, kafkaProducer)
      .withRouter(RoundRobinPool(5)).withDispatcher("workers-dispatcher"), name = "EventHandlerWorkers")

    this.executor = Executors.newFixedThreadPool(numberOfConsumers)
    this.consumers = List.fill(numberOfConsumers)(new EventReceiver(kafkaBootstrapServers, workers))
    consumers.foreach(executor.submit)
  }

}
