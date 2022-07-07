package com.rush.cloud.betslip;

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
        // you test your lambdas by invoking on http://localhost:8081
        // this works in dev mode too

//        Person in = new Person();
//        in.setName("Stu");
//        given()
//                .contentType("application/json")
//                .accept("application/json")
//                .body(in)
//                .when()
//                .post()
//                .then()
//                .statusCode(200)
//                .body(containsString("Hello Stu"));
    }

}
