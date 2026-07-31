# User Context Propagation Architecture

This document describes the enterprise architecture used in BiteBolt to propagate authenticated user details from the API Gateway downstream to all internal microservices securely and efficiently.

## 🚀 The Architecture

BiteBolt uses a **ThreadLocal + Interceptor** design pattern bundled inside the `common-security` module. This provides a plug-and-play Security Context for any downstream microservice without tightly coupling them to Spring Security or requiring them to re-verify JWT tokens.

### Flow
1. **API Gateway**: Validates the JWT token. If valid, extracts the User ID and Role, and injects them into the HTTP Request as headers (`X-User-Id` and `X-User-Role`).
2. **Microservice Interceptor (`UserContextInterceptor`)**: When the request reaches a microservice (e.g., `user-service`), this Spring MVC Interceptor reads the injected headers and constructs a `UserContext` object.
3. **ThreadLocal (`UserContextHolder`)**: The `UserContext` is stored in a `ThreadLocal` variable, making it globally accessible to the current HTTP Request thread.
4. **Argument Resolver (`CurrentUserArgumentResolver`)**: When a Controller method requests the `@CurrentUser`, this component resolves the parameter by pulling it from the `UserContextHolder`.

## 🛠 Usage Guide (For Developers)

To use the User Context in any microservice, simply ensure the microservice imports `common-security` in its `pom.xml`:
```xml
<dependency>
    <groupId>com.bitebolt</groupId>
    <artifactId>common-security</artifactId>
    <version>${project.version}</version>
</dependency>
```

Then, inject it directly into your Controller endpoints:
```java
@GetMapping("/me")
public ResponseEntity<?> getMyProfile(@CurrentUser UserContext context) {
    String userId = context.getUserId();
    String role = context.getRole();
    
    // Process business logic...
}
```

## ⚠️ Important Considerations
- **Memory Leaks**: `UserContextInterceptor` automatically calls `UserContextHolder.clear()` in the `afterCompletion` phase to prevent ThreadLocal memory leaks in Tomcat's thread pool.
- **Asynchronous Execution**: If you spawn a new thread (e.g., using `@Async` or `CompletableFuture`), the `UserContext` will **NOT** automatically propagate to the new thread. You must manually extract the context and pass it to the new thread if required.
- **Scope**: This module (`common-security`) uses Spring Web MVC and is only designed for traditional Servlet-based applications. It cannot be used in Spring WebFlux applications (like the API Gateway itself).
