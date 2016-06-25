package com.lucianomolinari.ap.eventreceiver

import java.util.Properties

import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer

object KafkaUtil {

  val pageViewTopic = "page-view-topic"
  val pageViewEnrichedTopic = "page-view-enriched-topic"

  def createKafkaProducer(kafkaBootstrapServers: String): KafkaProducer[String, String] = {
    val propsProducer = new Properties()
    propsProducer.put("bootstrap.servers", kafkaBootstrapServers)
    propsProducer.put("acks", "all")
    propsProducer.put("retries", "0")
    propsProducer.put("batch.size", "16384")
    propsProducer.put("linger.ms", "1")
    propsProducer.put("buffer.memory", "33554432")
    propsProducer.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    propsProducer.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    new KafkaProducer(propsProducer)
  }

  def createKafkaConsumer(kafkaBootstrapServers: String,
                          groupId: String = "page-view-receiver-group"): KafkaConsumer[String, String] = {
    val propsConsumer = new Properties()
    propsConsumer.put("bootstrap.servers", kafkaBootstrapServers)
    propsConsumer.put("group.id", groupId)
    propsConsumer.put("enable.auto.commit", "true")
    propsConsumer.put("auto.commit.interval.ms", "1000")
    propsConsumer.put("session.timeout.ms", "30000")
    propsConsumer.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    propsConsumer.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    new KafkaConsumer(propsConsumer)
  }

}
