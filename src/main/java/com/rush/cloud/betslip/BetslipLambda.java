package com.rush.cloud.betslip;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rush.cloud.betslip.builder.BetTypeBuilderFactory;
import com.rush.cloud.betslip.request.BetSlipImageGenerationRequest;

import software.amazon.lambda.powertools.validation.ValidationException;
import software.amazon.lambda.powertools.validation.ValidationUtils;

public class BetslipLambda implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final AmazonS3 S3_CLIENT = AmazonS3ClientBuilder.standard().withRegion(Regions.DEFAULT_REGION).build();
    private static final BetTypeBuilderFactory IMAGE_BUILDER_FACTORY = new BetTypeBuilderFactory();
    private static final String BUCKET_ENV_VAR = "BETSLIP_BUCKET";

    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent input, final Context context) {

        try {
            // Validate request body against json schema
            ValidationUtils.validate(input.getBody(), "classpath:/schema/request-schema.json");
            BetSlipImageGenerationRequest request = OBJECT_MAPPER.readValue(input.getBody(), BetSlipImageGenerationRequest.class);

            BufferedImage image = IMAGE_BUILDER_FACTORY
                    .getBuilder(request.getPlayType())
                    .buildImage(request);

            URL url = uploadImageToS3(image);

            String body = OBJECT_MAPPER.writeValueAsString(
                    Map.of(
                            "url", url,
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
                Map<String, List<Map<String, Object>>> validationMessage = OBJECT_MAPPER.readValue(e.getMessage(), new TypeReference<>() {});
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
        } catch (AmazonServiceException e) {
            String msg = e.getErrorMessage();
            return handleError(context.getAwsRequestId(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e, msg);
        } catch (Exception e) {
            String msg = e.getMessage();
            return handleError(context.getAwsRequestId(), HttpStatus.SC_INTERNAL_SERVER_ERROR, e, msg);
        }
    }

    private URL uploadImageToS3(BufferedImage image) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        byte[] byteArray = os.toByteArray();
        InputStream is = new ByteArrayInputStream(byteArray);

        String key = UUID.randomUUID() + ".png";
        ObjectMetadata ob = new ObjectMetadata();
        ob.setContentType(ContentType.IMAGE_PNG.getMimeType());
        ob.setContentLength(byteArray.length);

        String bucketName = System.getenv(BUCKET_ENV_VAR);
        PutObjectRequest putObjReq = new PutObjectRequest(bucketName, key, is, ob)
                .withCannedAcl(CannedAccessControlList.PublicRead);
        S3_CLIENT.putObject(putObjReq);

        return S3_CLIENT.getUrl(bucketName, key);
    }

    private APIGatewayV2HTTPResponse handleError(String requestId, int statusCode, Exception e, String ... messages) {
        e.printStackTrace();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("requestId", requestId);
        jsonMap.put("statusCode", statusCode);
        jsonMap.put("messages", messages);

        String errorMsg;
        try {
            errorMsg = OBJECT_MAPPER.writeValueAsString(jsonMap);
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
