package com.rush.cloud.betslip;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rush.cloud.betslip.builder.BetTypeBuilderFactory;
import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.SdkResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Slf4j
public class BetslipLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final String BUCKET_ENV_VAR = "BETSLIP_BUCKET";
    private final BetTypeBuilderFactory imgBuilderFactory;
    private final S3Client s3Client;
    private final Validator validator;
    private final ObjectMapper objectMapper;

    @Inject
    public BetslipLambda(BetTypeBuilderFactory imgBuilderFactory, S3Client s3Client, Validator validator) {
        this.imgBuilderFactory = imgBuilderFactory;
        this.s3Client = s3Client;
        this.validator = validator;
        this.objectMapper = new ObjectMapper();
    }

    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent input, final Context context) {

        try {
            BetSlipImageGenerationRequest request = objectMapper.readValue(input.getBody(), BetSlipImageGenerationRequest.class);
            Set<ConstraintViolation<BetSlipImageGenerationRequest>> constraints = validator.validate(request);
            if (!constraints.isEmpty()) {
                String[] messages = constraints.stream()
                        .map(c -> c.getPropertyPath() + " " + c.getMessage())
                        .toArray(String[]::new);
                return handleError(context.getAwsRequestId(), HttpStatus.SC_BAD_REQUEST, messages);
            }

            BufferedImage image = imgBuilderFactory
                    .getBuilder(request.getPlayTypeEnum())
                    .buildImage(request);

            return uploadImageToS3(image)
                    .map(url -> {
                        try {
                             return objectMapper.writeValueAsString(Map.of(
                                     "url", url,
                                     "requestId", context.getAwsRequestId()
                             ));
                         } catch (JsonProcessingException e) {
                             throw new RuntimeException(e);
                         }
                     })
                    .map(jsonBody -> APIGatewayV2HTTPResponse.builder()
                            .withHeaders(Map.of(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType()))
                            .withStatusCode(HttpStatus.SC_OK)
                            .withBody(jsonBody)
                            .build())
                    .orElseThrow(() -> new RuntimeException("Error occurred when uploading image to S3"));

        } catch (Exception e) {
            log.error("Error occurred when generating betslip image", e);
            return handleError(context.getAwsRequestId(), HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error occurred when generating betslip image");
        }
    }

    private Optional<URL> uploadImageToS3(BufferedImage image) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] byteArray = os.toByteArray();

        String bucketName = System.getenv(BUCKET_ENV_VAR);
        String key = UUID.randomUUID() + ".png";

        return Optional.ofNullable(
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .contentLength((long) byteArray.length)
                                .contentType(ContentType.IMAGE_PNG.getMimeType())
                                .acl(ObjectCannedACL.PUBLIC_READ)
                                .build(),
                        RequestBody.fromBytes(byteArray)
                ))
                .map(SdkResponse::sdkHttpResponse)
                .filter(SdkHttpResponse::isSuccessful)
                .map(sdkHttpResponse -> s3Client.utilities().getUrl(
                        GetUrlRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .build()));
    }

    private APIGatewayV2HTTPResponse handleError(String requestId, int statusCode, String ... messages) {

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
