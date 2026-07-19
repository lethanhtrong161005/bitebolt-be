package com.ecomove.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class AuthGatewayFilter extends AbstractGatewayFilterFactory<AuthGatewayFilter.Config> {

    public AuthGatewayFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            // 1. BYPASS LOGIC: Allow Swagger docs, Actuator endpoints, and Auth endpoint to pass through
            if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/actuator") || path.contains("/api/v1/auth")) {
                return chain.filter(exchange);
            }

            // 2. AUTH LOGIC (JWT Validation placeholder)
            // String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            // ... (Logic to parse and validate token if needed in future) ...

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // configuration properties if needed
    }
}
