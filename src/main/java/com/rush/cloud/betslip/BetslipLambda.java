package com.rush.cloud.betslip;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rush.cloud.betslip.builder.BetTypeBuilderFactory;
import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

//import software.amazon.awssdk.core.sync.RequestBody;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3Client;
//import software.amazon.awssdk.services.s3.model.GetUrlRequest;
//import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.lambda.powertools.validation.ValidationException;
import software.amazon.lambda.powertools.validation.ValidationUtils;

public class BetslipLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ObjectMapper objectMapper;
    private final BetTypeBuilderFactory imgBuilderFactory;
    private final String bucketEnvKey;
//    private final S3Client s3Client;
    public BetslipLambda() {
        objectMapper = new ObjectMapper();
        imgBuilderFactory = new BetTypeBuilderFactory();
        bucketEnvKey = "BETSLIP_BUCKET";
//        s3Client = S3Client.builder().region(Region.US_WEST_2).build();
    }

    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent input, final Context context) {

        try {
            // Validate request body against json schema
//            ValidationUtils.validate(input.getBody(), "classpath:/schema/request-schema.json");
            BetSlipImageGenerationRequest request = objectMapper.readValue(input.getBody(), BetSlipImageGenerationRequest.class);

            BufferedImage image = imgBuilderFactory
                    .getBuilder(request.getPlayType())
                    .buildImage(request);

            URL url = uploadImageToS3(image);

            String body = objectMapper.writeValueAsString(
                    Map.of(
                            "url", "https://example.com",
                            "requestId", context.getAwsRequestId()
                    )
            );

            return APIGatewayV2HTTPResponse.builder()
                    .withHeaders(Map.of(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
                    .withStatusCode(HttpStatus.SC_OK)
                    .withBody(body)
                    .build();

        } catch (ValidationException e) {
            try {
                Map<String, List<Map<String, Object>>> validationMessage = objectMapper.readValue(e.getMessage(), new TypeReference<>() {});
                List<Map<String, Object>> validationErrors = validationMessage.get("validationErrors");
                String[] messages = validationErrors.stream()
                        .map(validationError -> validationError.get("message"))
                        .map(Object::toString)
                        .toArray(String[]::new);
                return handleError(context.getAwsRequestId(), HttpStatus.SC_BAD_REQUEST, e, messages);
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
                return handleError(context.getAwsRequestId(), HttpStatus.SC_BAD_REQUEST, e, e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = e.getMessage();
            return handleError(context.getAwsRequestId(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e, msg);
        }
    }

    private URL uploadImageToS3(BufferedImage image) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] byteArray = os.toByteArray();

        String bucketName = System.getenv(bucketEnvKey);
        String key = UUID.randomUUID() + ".png";

        return null;

//        s3Client.putObject(PutObjectRequest.builder()
//                             .bucket(bucketName)
//                             .key(key)
//                             .contentLength((long) byteArray.length)
//                             .contentType(ContentType.IMAGE_PNG.getMimeType())
//                             .acl(ObjectCannedACL.PUBLIC_READ)
//                             .build(),
//                           RequestBody.fromBytes(byteArray));
//
//        return s3Client.utilities().getUrl(GetUrlRequest.builder()
//                                             .bucket(bucketName)
//                                             .key(key)
//                                             .build());
    }

    private APIGatewayV2HTTPResponse handleError(String requestId, int statusCode, Exception e, String ... messages) {
        e.printStackTrace();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("requestId", requestId);
        jsonMap.put("statusCode", statusCode);
        jsonMap.put("messages", messages);

        String errorMsg;
        try {
            errorMsg = objectMapper.writeValueAsString(jsonMap);
        } catch (JsonProcessingException jsonException) {
            errorMsg = String.join(",", messages);
        }

        return APIGatewayV2HTTPResponse.builder()
                .withHeaders(Map.of(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
                .withStatusCode(statusCode)
                .withBody(errorMsg)
                .build();
    }
}
