package com.rush.cloud.betslip.lambda;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.equalTo;

//@QuarkusTest
public class LambdaHandlerTest {

    //@Test
    public void testJaxrs() {
        RestAssured.when().post("/hello").then()
                .body(equalTo("hello jaxrs"));
    }

}
