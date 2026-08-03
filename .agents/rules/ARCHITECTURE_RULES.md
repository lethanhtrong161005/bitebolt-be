# 🏛️ BiteBolt Architecture Rules (MANDATORY)

**Status**: MANDATORY | **Version**: 2.0 | **Last Updated**: August 2024

> ⚠️ All microservices MUST follow these architectural patterns. Violations result in design review rejection.

---

## 📋 Table of Contents

1. [System Overview](#system-overview)
2. [Service Communication Patterns](#service-communication-patterns)
3. [Data Flow Patterns](#data-flow-patterns)
4. [Transaction Management](#transaction-management)
5. [Error Handling](#error-handling)
6. [Caching Strategy](#caching-strategy)
7. [Security Patterns](#security-patterns)
8. [Monitoring & Logging](#monitoring--logging)

---

## 1. System Overview

BiteBolt is a **Maven multi-module monorepo** with:

```
bitebolt-backend/
│
├── common-libraries/
│   ├── common-dto/              # Shared response wrappers, constants
│   ├── common-validation/       # Custom validation annotations
│   ├── common-exception/        # Global exception hierarchy
│   ├── common-logging/          # MDC, tracing utilities
│   ├── common-grpc/             # gRPC stubs and interceptors
│   └── common-security/         # JWT, encryption, UserContext
│
├── core-services/
│   ├── auth-service/            # Authentication & authorization
│   ├── user-service/            # User management
│   ├── booking-service/         # Business logic
│   ├── audit-service/           # Audit trail persistence
│   └── [other-services]/
│
├── api-gateway/                 # Central security gateway
│
└── .agents/                     # This framework
```

### Key Characteristics

- **Microservices**: Each service is independent, deployable, scalable
- **Shared Libraries**: Common code in `common-libraries/` to prevent duplication
- **API Gateway**: Single entry point for all external requests
- **Message-Driven**: Kafka for async event processing
- **Caching**: Redis for distributed caching and session storage
- **Distributed Tracing**: MDC + OpenTelemetry for request correlation

---

## 2. Service Communication Patterns

### ✅ Rule 2.1: Internal Service-to-Service Communication

**Use gRPC for internal calls between microservices.**

**WHY?**
- gRPC is type-safe with Protocol Buffers
- Binary format (not text) = faster and smaller
- Built-in streaming support
- Connection pooling and multiplexing
- Better performance than REST for high-frequency calls

**WRONG** ❌
```java
// ❌ DO NOT: Call another service via REST HTTP
RestTemplate restTemplate = new RestTemplate();
User user = restTemplate.getForObject("http://user-service:8080/api/v1/users/123", User.class);
```

**CORRECT** ✅
```java
// ✅ DO: Use gRPC stub
public class BookingService {
  private final UserServiceGrpc.UserServiceBlockingStub userStub;
  
  public void createBooking(CreateBookingRequest request) {
    // Call user service via gRPC
    GetUserResponse user = userStub.getUser(
        GetUserRequest.newBuilder()
            .setUserId(request.getUserId())
            .build()
    );
  }
}
```

### ✅ Rule 2.2: External Client Calls

**REST API (HTTP) for external clients only.**

```
External Client (Web/Mobile)
         ↓ (REST/HTTP)
    API Gateway
         ↓ (gRPC)
   Microservice
         ↓ (gRPC)
   Other Service
```

**REST Endpoints Structure**:
```
GET    /api/v1/users              # List all users
GET    /api/v1/users/{id}         # Get single user
POST   /api/v1/users              # Create user
PUT    /api/v1/users/{id}         # Update user
DELETE /api/v1/users/{id}         # Delete user
```

### ✅ Rule 2.3: Async Event Processing

**Use Kafka for asynchronous, event-driven communication.**

**When to use Kafka**:
- User registration event → Send welcome email
- Order created event → Update inventory
- Payment processed → Trigger fulfillment
- Any async notification or side effect

**WRONG** ❌
```java
// ❌ DO NOT: Synchronous calls for side effects
public void createUser(CreateUserRequest request) {
  User user = userRepository.save(request.toEntity());
  emailService.sendWelcomeEmail(user.getEmail());  // Blocks! Bad!
  return userMapper.toResponse(user);
}
```

**CORRECT** ✅
```java
// ✅ DO: Emit event, let Kafka handle side effects
@Service
public class UserService {
  private final UserRepository userRepository;
  private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
  
  @Transactional
  public UserResponse create(CreateUserRequest request) {
    User user = userRepository.save(request.toEntity());
    
    // Emit event asynchronously
    UserCreatedEvent event = new UserCreatedEvent(user.getId(), user.getEmail());
    kafkaTemplate.send("user.created", event);
    
    return userMapper.toResponse(user);  // Returns immediately
  }
}

// Separate service listens to event
@Service
public class EmailService {
  @KafkaListener(topics = "user.created")
  public void onUserCreated(UserCreatedEvent event) {
    // This runs asynchronously
    sendWelcomeEmail(event.getEmail());
  }
}
```

---

## 3. Data Flow Patterns

### ✅ Rule 3.1: Request-Response Flow

All requests follow this strict layering:

```
Client Request (HTTP/REST)
    ↓
API Gateway
  - Validate JWT
  - Inject X-User-Id, X-User-Role headers
  - Rate limiting
    ↓
Controller Layer
  - Request validation
  - Convert request DTO → service input
  - Call service
  - Convert response → response DTO
  - Return HTTP status + ApiResponse wrapper
    ↓
Service Layer
  - Business logic
  - Validation
  - Orchestration
  - Transaction management
    ↓
Repository Layer
  - Database queries only
  - No business logic
    ↓
Database
  - Persistence
```

### ✅ Rule 3.2: Response Wrapper Format

**All responses must be wrapped in ApiResponse.**

```java
public class ApiResponse<T> {
  private boolean success;
  private T data;
  private String message;
  private Map<String, String> errors;
}
```

**Example Response**:
```json
{
  "success": true,
  "data": {
    "id": 123,
    "name": "John Doe",
    "email": "john@bitebolt.com"
  },
  "message": "ENTITY_CREATED_SUCCESSFULLY",
  "errors": null
}
```

**Error Response**:
```json
{
  "success": false,
  "data": null,
  "message": "EMAIL_ALREADY_REGISTERED",
  "errors": {
    "email": "Email already exists in the system"
  }
}
```

### ✅ Rule 3.3: DTO Mapping

**Always use DTOs for request/response, never expose entities.**

```java
// ❌ WRONG: Exposing entity directly
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) {
  return userRepository.findById(id).orElse(null);  // Exposes password!
}

// ✅ CORRECT: Use DTO
@GetMapping("/{id}")
public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
  User user = userRepository.findById(id)
      .orElseThrow(() -> new NotFoundException("USER_NOT_FOUND"));
  return ApiResponse.success(userMapper.toResponse(user));
}

// UserResponse never includes sensitive fields
@Data
@Builder
public class UserResponse {
  private Long id;
  private String name;
  private String email;
  // NOT included: password, internalFlags, auditData
}
```

---

## 4. Transaction Management

### ✅ Rule 4.1: Transaction Boundaries

**Transactions should be at service layer, not repository.**

```java
// ✅ CORRECT: @Transactional at service
@Service
@Transactional
public class UserService {
  
  @Transactional(readOnly = true)
  public UserResponse getById(Long id) {
    return userRepository.findById(id);
  }
  
  @Transactional  // Explicit, read-write
  public UserResponse update(Long id, UpdateRequest request) {
    User user = userRepository.findById(id).orElseThrow();
    user.setName(request.getName());
    return userRepository.save(user);
  }
}

// ❌ WRONG: @Transactional in repository
@Repository
public class UserRepository extends JpaRepository<User, Long> {
  @Transactional  // Wrong place!
  public void save(User user) {
    // ...
  }
}
```

### ✅ Rule 4.2: Read-Only Transactions

**Mark query methods with @Transactional(readOnly = true) for optimization.**

```java
// ✅ CORRECT
@Transactional(readOnly = true)
public Page<UserResponse> getAll(Pageable pageable) {
  return userRepository.findAll(pageable)
      .map(userMapper::toResponse);
}
```

### ✅ Rule 4.3: Propagation Strategy

**Use default propagation (REQUIRED) for most cases.**

```java
// ✅ CORRECT: Default propagation
@Transactional
public void createUser(CreateUserRequest request) {
  // Joins existing transaction if present
  // Creates new transaction if none exists
}

// ⚠️ Use REQUIRES_NEW carefully
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void auditAction(String action) {
  // Always creates a NEW transaction
  // Useful for audit logging (should not fail main transaction)
}
```

---

## 5. Error Handling

### ✅ Rule 5.1: Exception Hierarchy

```
Throwable
  ├── Exception
  │   └── RuntimeException (preferred)
  │       └── HttpException (custom base for API errors)
  │           ├── BadRequestException (400)
  │           ├── UnauthorizedException (401)
  │           ├── ForbiddenException (403)
  │           ├── NotFoundException (404)
  │           ├── ConflictException (409)
  │           ├── ValidationException
  │           └── [Custom exceptions]
  │
  └── Error (system errors, never catch)
```

### ✅ Rule 5.2: Custom Exceptions

**Create domain-specific exceptions for business errors.**

```java
/**
 * Thrown when a user attempts to create an account with email
 * that is already registered in the system.
 * 
 * HTTP Status: 409 Conflict
 */
public class DuplicateUserException extends HttpException {
  
  public DuplicateUserException(String messageCode) {
    super(HttpStatus.CONFLICT, messageCode);
  }
  
  public DuplicateUserException(String messageCode, Throwable cause) {
    super(HttpStatus.CONFLICT, messageCode, cause);
  }
}
```

### ✅ Rule 5.3: Global Exception Handler

**All exceptions handled centrally via @RestControllerAdvice.**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
  
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ApiResponse<?>> handleNotFound(NotFoundException e) {
    return ResponseEntity
        .status(e.getHttpStatus())
        .body(ApiResponse.error(e.getMessageCode()));
  }
  
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiResponse<?>> handleBadRequest(BadRequestException e) {
    return ResponseEntity
        .status(e.getHttpStatus())
        .body(ApiResponse.error(e.getMessageCode()));
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<?>> handleUnexpected(Exception e) {
    logger.error("Unexpected error occurred", e);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("SYSTEM_ERROR"));
  }
}
```

---

## 6. Caching Strategy

### ✅ Rule 6.1: When to Cache

**Cache for data that**:
- Changes infrequently
- Is expensive to compute
- Is read frequently
- Can be slightly stale

### ✅ Rule 6.2: Redis Cache Usage

```java
@Service
@RequiredArgsConstructor
public class UserService {
  
  private final UserRepository userRepository;
  private final RedisTemplate<String, User> redisTemplate;
  
  /**
   * Get user, using cache when available.
   */
  @Cacheable(value = "users", key = "#id", unless = "#result == null")
  public User getById(Long id) {
    logger.debug("Cache miss for user: {}", id);
    return userRepository.findById(id).orElse(null);
  }
  
  /**
   * Update user and invalidate cache.
   */
  @CacheEvict(value = "users", key = "#id")
  @Transactional
  public User update(Long id, UpdateRequest request) {
    User user = getById(id);
    user.setName(request.getName());
    return userRepository.save(user);
  }
}
```

### ✅ Rule 6.3: Session Storage in Redis

```java
// Use Redis for session storage (not in-memory)
spring:
  session:
    store-type: redis  # NOT memory!
    redis:
      namespace: bitebolt:session
      flush-mode: on-save
```

---

## 7. Security Patterns

### ✅ Rule 7.1: JWT Token Validation

**API Gateway validates ALL tokens centrally.**

```
Client Request
     ↓ (with Authorization: Bearer <token>)
API Gateway
  ├─ Parse JWT
  ├─ Validate signature
  ├─ Check token not expired
  ├─ Check token not in blacklist (Redis)
  ├─ Extract user ID and role
  ├─ Inject X-User-Id and X-User-Role headers
  ↓
Microservice
  └─ Trust injected headers (never re-verify)
```

**Microservices TRUST gateway headers:**
```java
@RestController
public class UserController {
  
  @GetMapping("/{id}")
  public ApiResponse<UserResponse> getUser(
      @PathVariable Long id,
      @RequestHeader("X-User-Id") Long userId,
      @RequestHeader("X-User-Role") String role) {
    // Trust these headers from gateway
    // No need to re-verify JWT
    
    if (role.equals("ADMIN")) {
      return ApiResponse.success(userService.getById(id));
    } else if (userId.equals(id)) {
      return ApiResponse.success(userService.getById(id));
    } else {
      throw new ForbiddenException("FORBIDDEN");
    }
  }
}
```

### ✅ Rule 7.2: Role-Based Access Control

```java
// ✅ Use @Secured or custom authorizer
@RestController
public class AdminController {
  
  @GetMapping("/admin/users")
  @Secured("ADMIN")  // Only ADMIN role
  public ApiResponse<List<UserResponse>> getAll() {
    return ApiResponse.success(userService.getAll());
  }
  
  @GetMapping("/admin/audit")
  @Secured({"ADMIN", "STAFF"})  // Multiple roles
  public ApiResponse<List<AuditLog>> getAudit() {
    return ApiResponse.success(auditService.getLogs());
  }
}
```

### ✅ Rule 7.3: Soft Delete & Data Privacy

**Never physically delete sensitive data. Use soft delete.**

```java
@Entity
public class User {
  
  @Column(nullable = false, name = "is_deleted")
  private Boolean isDeleted = false;
  
  @Column(nullable = false, name = "deleted_at")
  private LocalDateTime deletedAt;
  
  // Repository filters automatically
  @Query("SELECT u FROM User u WHERE u.isDeleted = false")
  Page<User> findAll(Pageable pageable);
  
  // Deletion
  public void softDelete() {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
  }
}
```

---

## 8. Monitoring & Logging

### ✅ Rule 8.1: Structured Logging

**Use SLF4J + logstash-logback-encoder for JSON logging.**

```yaml
# logback-spring.xml
<appender name="jsonLogstash" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <customFields>{"service":"user-service","environment":"prod"}</customFields>
  </encoder>
</appender>
```

**Log output becomes JSON**:
```json
{
  "@timestamp": "2024-08-03T10:30:00Z",
  "level": "INFO",
  "logger_name": "com.bitebolt.user.service.UserService",
  "message": "User created successfully",
  "userId": "123",
  "traceId": "abc-def-ghi",
  "service": "user-service"
}
```

### ✅ Rule 8.2: Distributed Tracing

**Include traceId in all logs across services.**

```java
@Component
public class TraceIdFilter extends OncePerRequestFilter {
  
  @Override
  protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response,
                                   FilterChain chain) {
    String traceId = UUID.randomUUID().toString();
    MDC.put("traceId", traceId);
    
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove("traceId");
    }
  }
}
```

**Now all logs include traceId**:
```java
logger.info("Processing request");  // Includes traceId automatically
logger.info("Calling user service");  // Includes traceId
logger.info("Response sent");  // Includes traceId
```

### ✅ Rule 8.3: Audit Logging

**Track important state changes using @Auditable.**

```java
@Service
public class UserService {
  
  @Auditable(action = AuditAction.CREATE, entity = "User")
  @Transactional
  public UserResponse create(CreateUserRequest request) {
    // Changes are automatically logged to audit service
  }
  
  @Auditable(action = AuditAction.UPDATE, entity = "User")
  @Transactional
  public UserResponse update(Long id, UpdateRequest request) {
    // Changes are automatically logged to audit service
  }
  
  @Auditable(action = AuditAction.DELETE, entity = "User")
  @Transactional
  public void delete(Long id) {
    // Changes are automatically logged to audit service
  }
}
```

---

## ✅ Architecture Checklist

Before deploying a new service, verify:

- [ ] Service communicates with others via **gRPC** (not REST)
- [ ] API responses wrapped in **ApiResponse** consistently
- [ ] Request/response use **DTOs**, not entities
- [ ] Transactions defined at **service layer**, not repository
- [ ] Custom **exceptions** inherit from HttpException
- [ ] **Global exception handler** installed
- [ ] Error codes defined in **MessageConstant**
- [ ] Read-heavy queries use **@Transactional(readOnly = true)**
- [ ] Sensitive data never **logged**
- [ ] **Soft delete** pattern used for entities
- [ ] Audit logging with **@Auditable** for state changes
- [ ] **Redis** used for caching and sessions
- [ ] **Kafka** used for async events
- [ ] **Structured JSON logging** with logstash
- [ ] **Trace IDs** in MDC for correlation

---

**Effective Date**: August 2024  
**Version**: 2.0  
**Status**: MANDATORY

