
```markdown
# TỔNG HỢP CẤU HÌNH VÀ ĐẶC TẢ ECO-MOVE (PHASE: GATEWAY, USER, AUTH)

Tài liệu này chứa toàn bộ cấu hình hạ tầng, API Gateway, User Service để thông luồng request và tích hợp Swagger UI tập trung, kèm theo đặc tả logic đăng nhập 2FA của Auth Service.

---

## 1. INFRASTRUCTURE (DOCKER COMPOSE)
**File:** `infrastructure/docker-compose.yml`
**Mục đích:** Khởi chạy các công cụ nền tảng nội bộ (Database, Cache, Message Broker).

```yaml
version: '3.8'

services:
  # 1. PostgreSQL cho User Service & Auth Service
  postgres-user:
    image: postgres:15-alpine
    container_name: ecomove-postgres-user
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: rootpassword
      POSTGRES_DB: user_db # Sẽ tạo thêm auth_db sau
    ports:
      - "5432:5432"
    volumes:
      - postgres_user_data:/var/lib/postgresql/data
    restart: unless-stopped

  # 2. Redis cho Caching & OTP
  redis:
    image: redis:7-alpine
    container_name: ecomove-redis
    ports:
      - "6379:6379"
    restart: unless-stopped

  # 3. Zookeeper (Bắt buộc cho Kafka)
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    container_name: ecomove-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  # 4. Kafka Message Broker
  kafka:
    image: confluentinc/cp-kafka:7.4.0
    container_name: ecomove-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

volumes:
  postgres_user_data:

```

---

## 2. API GATEWAY SETUP

### 2.1 Dependencies

**File:** `api-gateway/pom.xml` (Bổ sung vào thẻ `<dependencies>`)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-gateway-webflux-ui</artifactId>
    <version>2.5.0</version>
</dependency>

```

### 2.2 Application Properties

**File:** `api-gateway/src/main/resources/application.properties`
**Mục đích:** Cấu hình Route tự động và gom Swagger từ User Service.

```properties
spring.application.name=api-gateway
server.port=8080

# Đăng ký với Eureka Server (Discovery)
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Bật tính năng định tuyến tự động từ Eureka
spring.cloud.gateway.discovery.locator.enabled=true

# ---------------------------------------------------------
# ROUTING CONFIGURATION (Chỉ giữ lại User Service)
# ---------------------------------------------------------
spring.cloud.gateway.routes[0].id=user-service
spring.cloud.gateway.routes[0].uri=lb://user-service
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/users/**
# Áp dụng bộ lọc xác thực tùy chỉnh
spring.cloud.gateway.routes[0].filters[0].name=AuthGatewayFilter

# ---------------------------------------------------------
# SWAGGER AGGREGATOR CONFIGURATION
# ---------------------------------------------------------
springdoc.api-docs.enabled=true
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.config-url=/v3/api-docs/swagger-config

# Gom tài liệu từ User Service
springdoc.api-docs.groups.enabled=true
springdoc.swagger-ui.urls[0].url=/v3/api-docs/user-service
springdoc.swagger-ui.urls[0].name=User Service

```

### 2.3 Auth Filter (Bypass Swagger)

**File:** `api-gateway/.../AuthGatewayFilter.java`
**Mục đích:** Đảm bảo Gateway không chặn các request đòi xem tài liệu API.

```java
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

            // 1. BYPASS LOGIC: Cho phép các Endpoint của Swagger và Docs đi qua không cần Token
            if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
                return chain.filter(exchange);
            }

            // 2. AUTH LOGIC: Xử lý kiểm tra JWT Token ở đây cho các request nghiệp vụ
            // String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            // ... (Logic kiểm tra token của bạn) ...

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Cấu hình properties cho filter nếu cần
    }
}

```

---

## 3. USER SERVICE SETUP

### 3.1 Dependencies

**File:** `user-service/pom.xml` (Bổ sung vào thẻ `<dependencies>`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>

```

### 3.2 Application Properties

**File:** `user-service/src/main/resources/application.properties`
**Mục đích:** Khai báo database và hiển thị OpenAPI doc.

```properties
spring.application.name=user-service
server.port=8081

# Đăng ký với Eureka Server
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.instance.prefer-ip-address=true

# Cấu hình PostgreSQL (Khớp với Docker Compose)
spring.datasource.url=jdbc:postgresql://localhost:5432/user_db
spring.datasource.username=postgres
spring.datasource.password=rootpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Cấu hình Swagger cho service con
springdoc.api-docs.path=/v3/api-docs/user-service
springdoc.swagger-ui.path=/swagger-ui.html

```

---

## 4. AUTH SERVICE - 2FA LOGIN SPECIFICATION

### 4.1. DATABASE SEGREGATION (BOUNDED CONTEXT)

**`auth_db` (Auth Service)**
Store strictly login credentials.

* `credentials` table:
* `id` (UUID, PK)
* `user_id` (UUID, Ref to user_db)
* `phone` (String, Unique, Index)
* `password_hash` (String, Bcrypt)
* `role` (Enum: RIDER, DRIVER, ADMIN)
* `status` (Enum: ACTIVE, LOCKED, BANNED)

thêm base entiy các field như sau:
created_at
updated_at
created_by
updated_by
isDeleted



**`user_db` (User Service)**
Store business profiles (Handled separately via Saga on Register).

* `users` table: `user_id` (PK), `full_name`, `avatar`, `email`, `kyc_status`, `rating`.

thêm base entiy các field như sau:
created_at
updated_at
created_by
updated_by
isDeleted

### 4.2. API CONTRACTS

**POST `/api/v1/auth/login**`

* **Purpose:** Verify phone + password, generate OTP, save to Redis, publish Kafka Event.
* **Request:** `{"phone": "0987654321", "password": "RawPassword123"}`
* **Response (200 OK):** `{"session_id": "uuid-v4-string"}`

**POST `/api/v1/auth/verify-otp**`

* **Purpose:** Validate OTP from Redis, generate JWT, delete OTP.
* **Request:** `{"session_id": "uuid-v4-string", "otp": "123456"}`
* **Response (200 OK):** `{"access_token": "jwt-string", "refresh_token": "jwt-string"}`

### 4.3. BUSINESS LOGIC & PSEUDO-CODE

**Login Flow (First Factor)**

```text
FUNCTION login(phone, password):
  1. credential = AuthRepository.findByPhone(phone)
  2. IF NOT credential OR NOT Bcrypt.match(password, credential.password_hash):
        THROW UnauthorizedException("Invalid phone or password")
  3. IF credential.status != ACTIVE:
        THROW ForbiddenException("Account is locked")
        
  4. otp_code = GenerateRandom(6_digits)
  5. session_id = GenerateUUID()
  
  6. Redis.save(key: "OTP:" + session_id, value: otp_code, TTL: 5_minutes)
  7. Redis.save(key: "ATTEMPTS:" + session_id, value: 0, TTL: 5_minutes)
  
  8. KafkaProducer.send(topic: "sms_otp_events", payload: {phone: credential.phone, otp: otp_code})
  
  9. RETURN { "session_id": session_id }

```

**Verify OTP Flow (Second Factor)**

```text
FUNCTION verifyOtp(session_id, input_otp):
  1. attempts = Redis.get("ATTEMPTS:" + session_id)
  2. IF attempts >= 5:
        THROW TooManyRequestsException("Session blocked due to brute-force")
        
  3. cached_otp = Redis.get("OTP:" + session_id)
  4. IF NOT cached_otp:
        THROW BadRequestException("OTP expired or invalid session")
        
  5. IF input_otp != cached_otp:
        Redis.increment("ATTEMPTS:" + session_id)
        THROW UnauthorizedException("Invalid OTP")
        
  6. Redis.delete("OTP:" + session_id)
  7. Redis.delete("ATTEMPTS:" + session_id)
  
  8. credential = AuthRepository.findBySessionId_Mapping(session_id) // Logic depends on how you map session to user
  
  9. access_token = JwtUtils.generateAccessToken(credential.user_id, credential.role)
  10. refresh_token = JwtUtils.generateRefreshToken(credential.user_id)
  11. Redis.save(key: "RT:" + credential.user_id, value: hash(refresh_token), TTL: 7_days)
  
  12. RETURN { "access_token": access_token, "refresh_token": refresh_token }

```

### 4.4. SECURITY & JWT OPTIMIZATION

* **Bypass Gateway:** Ensure `/api/v1/auth/**` is bypassed in `api-gateway` AuthFilter.
* **JWT Payload:** Keep it lightweight. Only include `sub` (`user_id`), `role`, `iat`, `exp`. Do NOT include PII (phone, email) in the token to optimize token size and reduce network overhead.
* **Brute-Force Protection:** The 5-attempt limit in Redis is mandatory.
* **Replay Attack Protection:** OTP key in Redis must be explicitly deleted immediately upon successful verification.

```

```