FROM java:8

ADD target/scala-2.11/event-receiver.jar /app/event-receiver.jar

CMD java -jar /app/event-receiver.jar $KAFKA_SERVERS $CUSTOMER_APP_SERVER $NUMBER_OF_CONSUMERS