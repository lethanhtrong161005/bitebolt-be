package com.bitebolt.gateway.security;

import com.bitebolt.common.dto.ApiResponse;
import com.bitebolt.common.utils.MessageUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Helper class for generating standardized error responses in the API Gateway.
 * 
 * <p>Since Spring Cloud Gateway is built on Spring WebFlux (Reactor), we cannot use
 * standard Spring MVC `ResponseEntity`. Instead, this class manually constructs the 
 * standard `ApiResponse` JSON and writes it directly to the reactive `ServerHttpResponse` 
 * using a `DataBuffer`.
 * 
 * @author bitebolt Team
 */
@Component
public class GatewayErrorHelper {

    private final ObjectMapper objectMapper;

    public GatewayErrorHelper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Constructs and writes a 401 Unauthorized JSON response to the client.
     * 
     * @param response the current reactive ServerHttpResponse
     * @param messageCode the translation key (e.g., "ERROR_TOKEN_BLACKLISTED")
     * @return a Mono<Void> indicating when the response writing is complete
     */
    public Mono<Void> writeUnauthorized(ServerHttpResponse response, String messageCode) {
        // 1. Set HTTP status and content type
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 2. Build the standard ApiResponse envelope
        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .status(401)
                .message(MessageUtils.getMessage(messageCode))
                .traceRequest(null)
                .time(java.time.Instant.now())
                .build();
        
        try {
            // 3. Serialize and write to the response buffer
            byte[] bytes = objectMapper.writeValueAsBytes(apiResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            byte[] bytes = "{\"status\":401,\"message\":{\"code\":\"ERROR_UNAUTHORIZED\"}}".getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        }
    }
}
