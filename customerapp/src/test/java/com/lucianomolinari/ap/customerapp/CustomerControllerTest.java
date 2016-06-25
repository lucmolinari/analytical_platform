package com.lucianomolinari.ap.customerapp;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.jayway.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = CustomerApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:0")
public class CustomerControllerTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Value("${local.server.port}")
    private int port;

    @Before
    public void beforeTestCase() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        RestAssured.port = port;
    }

    @Test
    public void should_insert_and_find_customer() {
        given().
                contentType(ContentType.JSON).
                body(getJsonCustomer()).
        when().
                post("/customer").
        then().
                statusCode(HttpStatus.CREATED.value()).
                body("id", equalTo(1));

        when().
                get("/customer/1").
        then().
                statusCode(HttpStatus.OK.value()).
                body("id", equalTo(1)).
                body("name", equalTo("John")).
                body("gender", equalTo("MALE"));

    }

    @Test
    public void should_return_404_when_customer_not_found() {
        when().
                get("/customer/1").
        then().
                statusCode(HttpStatus.NOT_FOUND.value());

    }

    private String getJsonCustomer() {
        return String.format("{\"name\":\"%s\", \"gender\":\"%s\"}", "John", "MALE");
    }
}
