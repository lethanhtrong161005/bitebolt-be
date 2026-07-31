# Backend Logout & Token Blacklist Implementation Plan

To implement a secure, enterprise-grade logout mechanism, we must invalidate both the **Refresh Token** (preventing future renewals) and the **Access Token** (preventing immediate unauthorized access). Since JWTs are stateless, we will use a **Redis-backed Blacklist with auto-expiring TTLs**.

## Proposed Changes

### 1. Redis Configuration & Constants
#### [MODIFY] [AuthRedisConstant.java](file:///d:/workspace/BiteBolt/bitebolt-backend/core-services/auth-service/src/main/java/com/bitebolt/auth/constant/AuthRedisConstant.java)
- Add a new prefix for the token blacklist:
  ```java
  public static final String BLACKLIST_TOKEN_PREFIX = "BLACKLIST:AT:";
  ```

### 2. Service Logic
#### [MODIFY] [AuthService.java](file:///d:/workspace/BiteBolt/bitebolt-backend/core-services/auth-service/src/main/java/com/bitebolt/auth/service/AuthService.java)
- Declare `void logout(String accessToken, String clientTypeHeader, HttpServletResponse httpResponse);`

#### [MODIFY] [AuthServiceImpl.java](file:///d:/workspace/BiteBolt/bitebolt-backend/core-services/auth-service/src/main/java/com/bitebolt/auth/service/impl/AuthServiceImpl.java)
- Implement `logout` method:
  1. Validate the `accessToken`.
  2. Extract `userId` and token `expiration` date from claims.
  3. **Refresh Token Invalidation**: Delete the active refresh token hash from Redis (`RT:<userId>`).
  4. **Access Token Blacklisting**: Calculate the remaining TTL (Time-To-Live) of the `accessToken` in seconds (Expiration Date - Current Date). If > 0, store the hashed `accessToken` in Redis with the key `BLACKLIST:AT:<hash>` and the remaining TTL.
  5. If `ClientType` is `WEB`, overwrite the `access_token` and `refresh_token` cookies with `Max-Age=0` to clear them from the browser.
- Update `getProfileFromCookie()` to throw `401` if the provided token exists in the Redis Blacklist.

### 3. Controller Endpoint
#### [MODIFY] [AuthController.java](file:///d:/workspace/BiteBolt/bitebolt-backend/core-services/auth-service/src/main/java/com/bitebolt/auth/controller/AuthController.java)
- Add a new endpoint: `POST /api/v1/auth/logout`.
- Accept `@CookieValue("access_token")` or `@RequestHeader("Authorization")` (to support both Web and Mobile).
- Accept `@RequestHeader("Client-Type")`.
- Call `authService.logout(...)`.
- Return `ApiResponse` with success message (`SUCCESS_LOGOUT`).

### 4. Localization Messages
#### [MODIFY] messages.properties (and _en, _vi)
- Add `SUCCESS_LOGOUT` and `ERROR_TOKEN_BLACKLISTED`.

## System Design Notes (Enterprise Level)
- **Memory Efficiency**: By setting the Redis TTL to exactly the remaining lifespan of the JWT, we guarantee that Redis keys will automatically clean themselves up the exact moment the JWT natively expires anyway. The Blacklist will never bloat over time.
- **Security Check**: For now, the Blacklist check is added to the `/me` endpoint. In the future, this Redis check should be injected directly into the API Gateway (`AuthGatewayFilter.java`) so that *all* microservices are protected from blacklisted tokens without needing to check it themselves.

## Verification Plan

### Automated Tests
- Run maven compile for `auth-service` to ensure zero syntax errors.

### Manual Verification
1. Login to receive an Access Token and Refresh Token.
2. Call `POST /logout` with the Access Token.
3. Check Redis to verify the `BLACKLIST:AT:<hash>` key was created and `RT:<userId>` was deleted.
4. Attempt to call `/me` with the *same* Access Token -> Should fail with 401 Unauthorized (Blacklisted).
5. Wait for the token's original expiration time to pass -> Check Redis to ensure the blacklist key auto-deleted.
