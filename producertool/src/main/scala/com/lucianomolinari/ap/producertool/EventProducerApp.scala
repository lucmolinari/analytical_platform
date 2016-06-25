package com.lucianomolinari.ap.producertool

/**
  * Testing tool used for sending events to page-view-topic.
  */
object EventProducerApp {

  private val usage =
    """Usage.
      |The following arguments must be provided:
      |
      | - Number of messages to be sent per thread
      | - Number of threads to be created
      | - Interval in ms between each batch of messages
      | - Number of batches to be sent. (-1 to run indefinitely)
      | - Max number of users in the system. Needed to fill userId field
      | - Max number of products in the system. Needed to fill page field
      | - Kafka host/port on format host:port
    """.stripMargin

  private val argumentsProvided =
    """
      |Running producer with:
      | - Number of messages to be sent per thread: %s
      | - Number of threads to be created: %s
      | - Interval in ms between each batch of messages: %s
      | - Number of batches: %s
      | - Max number of users in the system: %s
      | - Max number of products in the system: %s
      | - Kafka host/port on format host:port: %s
    """.stripMargin

  def main(args: Array[String]): Unit = {
    if (args.length != 7) {
      println(usage)
      System.exit(1)
    }
    val config = EventProducerConfig(args(0).toInt, args(1).toInt, args(2).toLong, args(3).toInt, args(4).toInt,
      args(5).toInt, args(6))
    println(argumentsProvided.format(config.msgsPerThread, config.numberOfThreads, config.intervalInMs, config.batches,
      config.maxUsers, config.maxProducts, config.kafkaHost))

    new EventProducer(config).run
  }

}
