package com.rush.cloud.betslip;

import java.util.Map;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BetslipLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent input, Context context) {

        String body = null;
        try {
            body = OBJECT_MAPPER.writeValueAsString(
                    Map.of(
                            "url", "https://www.sample.com",
                            "requestId", context.getAwsRequestId()
                    )
            );
        } catch (JsonProcessingException e) {
            return null;
        }

        return APIGatewayV2HTTPResponse.builder()
                .withHeaders(Map.of(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
                .withStatusCode(HttpStatus.SC_OK)
                .withBody(body)
                .build();
    }
}
