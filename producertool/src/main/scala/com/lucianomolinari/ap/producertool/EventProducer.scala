package com.lucianomolinari.ap.producertool

import java.util.Properties
import java.util.concurrent.Executors

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Random

case class EventProducerConfig(val msgsPerThread: Int, val numberOfThreads: Int, val intervalInMs: Long,
                               val batches: Int, val maxUsers: Int, val maxProducts: Int, val kafkaHost: String)

class EventProducer(config: EventProducerConfig) {

  val propsProducer = new Properties()
  propsProducer.put("bootstrap.servers", config.kafkaHost)
  propsProducer.put("acks", "all")
  propsProducer.put("retries", "0")
  propsProducer.put("batch.size", "16384")
  propsProducer.put("linger.ms", "1")
  propsProducer.put("buffer.memory", "33554432")
  propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  // Needed to run the Futures
  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(config.numberOfThreads))

  // Thread safe, can be shared among all threads
  private val producer: KafkaProducer[String, String] = new KafkaProducer(propsProducer)

  val log = LoggerFactory.getLogger(getClass)

  val json =
    """
      | {
      |     "userId": %d,
      |     "timestamp": %d,
      |     "page": "/products/%s"
      | }
    """.stripMargin

  def run(): Unit = {
    val numberOfBatches = if (config.batches >= 0) config.batches else Int.MaxValue
    (1 to numberOfBatches).foreach(batch => {
      log.info(s"Sending batch $batch/$numberOfBatches")
      sendBatchAndBlockUntilCompletion(batch)
      log.info(s"Batch $batch/$numberOfBatches completed.")

      if (batch < numberOfBatches) {
        log.info(s"Waiting ${config.intervalInMs}ms until next batch")
        Thread sleep config.intervalInMs
      }
    })

    producer.close
    ec.shutdown
  }

  def sendBatchAndBlockUntilCompletion(batch: Int): Unit = {
    val futures = (1 to config.numberOfThreads) map { thread =>
      Future {
        log.info(s"Sending messages on thread $thread")
        (1 to config.msgsPerThread).foreach(i => {
          producer.send(new ProducerRecord[String, String]("page-view-topic", getJson))
        })
      }
    }
    futures.foreach(Await.ready(_, Duration.Inf))
  }

  def getJson(): String = {
    val userId = Random.nextInt(config.maxUsers) + 1
    val productId = Random.nextInt(config.maxProducts) + 1
    json.format(userId, System.currentTimeMillis, productId)
  }

}
