FROM java:8

ADD target/customer-app.jar /app/customer-app.jar

CMD java -jar /app/customer-app.jar --redis.host=$REDIS_HOST

EXPOSE 8080