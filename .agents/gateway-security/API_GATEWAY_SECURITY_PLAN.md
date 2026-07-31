# API Gateway & Security Centralization Plan

This document outlines the architecture and implementation of the centralized security layer in the **BiteBolt API Gateway**.

## ⚠️ Architectural Concept
- **Shift-Left Security**: The Gateway intercepts every request and takes full responsibility for parsing JWTs and querying the Redis Blacklist.
- **Header Injection**: If the token is valid, the Gateway injects `X-User-Id` and `X-User-Role` into the HTTP headers before forwarding the request downstream.
- **Decoupling**: Downstream services (e.g., `user-service`, `booking-service`) fully trust these injected headers and no longer need to parse JWTs or communicate with Redis for blacklist checks.

## Implementation Details

### 1. Dependencies & Shared Constants
- The `api-gateway` project imports `common-dto` (for standard `ApiResponse`), `jjwt` (for JWT validation), and `spring-boot-starter-data-redis-reactive` (for non-blocking Redis access).
- Both `auth-service` and `api-gateway` share the global constant `AppConstant.BLACKLIST_TOKEN_PREFIX` (`BLACKLIST:AT:`).

### 2. Gateway Security Components
#### `GatewayJwtVerifier.java`
- A lightweight Spring component that loads the symmetric `SecretKey` and parses inbound JWTs safely, explicitly handling `ExpiredJwtException`.
- Independent of heavy `auth-service` dependencies.

#### `GatewayErrorHelper.java`
- Since the Gateway uses Spring WebFlux (Reactor), this helper manually constructs the standard `ApiResponse` JSON and writes it directly to the reactive `ServerHttpResponse` using a `DataBuffer`.
- **Consistency**: Uses Spring's global `ObjectMapper` to ensure proper `Instant` ISO 8601 formatting and `NON_NULL` serialization rules.
- **i18n Support**: Utilizes `MessageUtils` with `messages.properties` and `messages_vi.properties` inside the gateway to provide standardized localized error messages (`ERROR_UNAUTHORIZED`, `ERROR_MISSING_TOKEN`, `ERROR_INVALID_TOKEN`, `ERROR_TOKEN_BLACKLISTED`).

#### `AuthGatewayFilterFactory.java`
- The core Gateway filter. Renamed with the `GatewayFilterFactory` suffix to strictly comply with Spring Cloud Gateway's automated mapping conventions (mapped as `name=Auth` in properties).
- It is fully reactive using Reactor (Mono).
- **Extraction**: Intelligently extracts tokens from either `Authorization: Bearer` (Mobile) or the `access_token` Cookie (Web).
- **Blacklist Check**: Hashes the token (SHA-256) and performs a reactive check against Redis. Blocks the request immediately if found in the blacklist.
- **Validation**: Uses `GatewayJwtVerifier` to ensure the token signature and expiration are valid.
- **Header Injection**: Extracts `userId` and `role` from the claims, mutates the incoming HTTP request, and attaches them as headers before forwarding.

## Verification & Dual-Client Handling
- When a user logs out (`POST /logout`), the `auth-service` places the SHA-256 hash of their access token into the Redis Blacklist with a TTL matching the token's remaining lifespan.
- Any subsequent requests with that token will be intercepted by the Gateway and rejected with `ERROR_TOKEN_BLACKLISTED` without ever reaching the downstream services.
- **Dual-Client Auth Mechanism**: The system automatically adapts authentication behavior. If `X-Client-Type: web` is sent, `auth-service` returns tokens via `Set-Cookie` (HttpOnly), and the Gateway reads the token from the `Cookie` header. Otherwise, it defaults to mobile and tokens are delivered in JSON, to be passed in the `Authorization: Bearer` header.
