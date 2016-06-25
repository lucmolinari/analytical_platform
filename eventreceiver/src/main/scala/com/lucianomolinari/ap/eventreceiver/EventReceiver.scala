package com.lucianomolinari.ap.eventreceiver

import java.util.concurrent.atomic.AtomicBoolean

import akka.actor.ActorRef
import org.apache.kafka.clients.consumer.{ConsumerRecords, KafkaConsumer}
import org.apache.kafka.common.errors.WakeupException

import scala.collection.JavaConversions._

/**
  * Responsible for receiving the raw events and dispatch them to a worker.
  */
class EventReceiver(kafkaBootstrapServers: String, workers: ActorRef) extends Runnable {

  val closed = new AtomicBoolean(false)
  val consumer: KafkaConsumer[String, String] = KafkaUtil.createKafkaConsumer(kafkaBootstrapServers)
  consumer.subscribe(java.util.Arrays.asList(KafkaUtil.pageViewTopic))

  override def run = {
    try {
      while (!closed.get()) {
        val records: ConsumerRecords[String, String] = consumer.poll(100)
        records.iterator().foreach(record => workers ! record.value)
      }
    } catch {
      case e: WakeupException => if (!closed.get()) throw e
    } finally {
      if (consumer != null) {
        consumer.close
      }
    }
  }

  def shutdown() = {
    closed.set(true)
    consumer.wakeup()
  }

}
