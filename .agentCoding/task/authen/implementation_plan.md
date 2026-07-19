# Implementation Plan: Gateway + User Service + Auth Service 2FA Login

## Tổng Quan

Task này implement 3 phần chính theo tài liệu `task-login-config.md`:
1. **Infrastructure** – Thêm PostgreSQL vào `docker-compose.yml`
2. **API Gateway** – Cấu hình routes, Swagger aggregator, `AuthGatewayFilter`
3. **User Service** – Cấu hình DB + Swagger
4. **Auth Service** – Implement 2FA login flow đầy đủ (Entity, Repository, Service, Controller, JWT, Redis, Kafka)

---

## Trạng Thái Hiện Tại (As-Is)

| Component | Trạng thái | Ghi chú |
|---|---|---|
| `infrastructure/docker-compose.yml` | ⚠️ Thiếu PostgreSQL | Chỉ có Redis, Kafka, Kafka-UI |
| `eco-move-config/gateway.properties` | ⚠️ Thiếu filter + Swagger | Có route cơ bản nhưng thiếu AuthFilter + Swagger config |
| `eco-move-config/auth-service.properties` | ⚠️ Password chưa đúng | `your_password` chưa set, thiếu Redis/Kafka/JWT config |
| `eco-move-config/user-service.properties` | ⚠️ Thiếu DB config | Chưa có datasource, Swagger |
| `api-gateway/pom.xml` | ⚠️ Thiếu Springdoc | Thiếu `springdoc-openapi-starter-gateway-webflux-ui` |
| `api-gateway` source | ❌ Trống | Chỉ có `ApiGatewayApplication.java` |
| `user-service` source | ⚠️ Skeleton | Có `UserController.java` nhưng chưa có entity/DB |
| `auth-service` source | ❌ Trống | Chỉ có `AuthServiceApplication.java` |
| `common-libraries` | ✅ Có sẵn | `common-dto`, `common-exception`, `common-logging`, `common-validation` |

---

## Open Questions

> [!IMPORTANT]
> **Câu hỏi 1:** `auth-service.properties` đang có `spring.datasource.password=your_password`. Password thực tế là gì? (Dựa trên docker-compose task spec là `rootpassword` – sẽ dùng giá trị này nếu không có phản hồi khác.)

> [!IMPORTANT]
> **Câu hỏi 2:** JWT secret key sẽ dùng giá trị hardcode tạm hay đọc từ environment variable? (Plan sẽ dùng placeholder `${jwt.secret}` trong config, cần set giá trị trong `auth-service.properties`.)

> [!IMPORTANT]
> **Câu hỏi 3:** Auth service có cần dùng `common-dto`, `common-exception`, `common-logging` như user-service không? (Plan giả định: **Có** – để đồng nhất response format.)

---

## Proposed Changes

### 1. Infrastructure

---

#### [MODIFY] [docker-compose.yml](file:///d:/workspace/EcoMove/eco-move-backend/infrastructure/docker-compose.yml)

Thêm service `postgres-user` (chứa cả `user_db` và `auth_db`) với init script:

```yaml
postgres-user:
  image: postgres:15-alpine
  container_name: ecomove-postgres-user
  environment:
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: rootpassword
    POSTGRES_DB: user_db
  ports:
    - "5432:5432"
  volumes:
    - postgres_user_data:/var/lib/postgresql/data
    - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
  networks:
    - ecomove-network
  restart: unless-stopped
```

#### [NEW] `infrastructure/init-db.sql`

Script tạo `auth_db` trong cùng PostgreSQL instance:
```sql
CREATE DATABASE auth_db;
```

---

### 2. Config Server (`eco-move-config`)

---

#### [MODIFY] [gateway.properties](file:///d:/workspace/EcoMove/eco-move-backend/eco-move-config/gateway.properties)

- Fix route `auth-service` index từ `[2]` → `[1]`
- Thêm `AuthGatewayFilter` vào route `user-service`
- Thêm config Swagger aggregator (urls `user-service`, `auth-service`)
- Thêm `springdoc` config

#### [MODIFY] [auth-service.properties](file:///d:/workspace/EcoMove/eco-move-backend/eco-move-config/auth-service.properties)

- Sửa `password=rootpassword`
- Thêm `spring.jpa.hibernate.ddl-auto=validate`
- Thêm Redis config (`spring.data.redis.host`, `port`)
- Thêm Kafka producer config (`spring.kafka.bootstrap-servers`)
- Thêm JWT config (`jwt.secret`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration`)
- Thêm Springdoc config (`springdoc.api-docs.path=/v3/api-docs/auth-service`)

#### [MODIFY] [user-service.properties](file:///d:/workspace/EcoMove/eco-move-backend/eco-move-config/user-service.properties)

- Thêm datasource config (PostgreSQL `user_db`)
- Thêm `spring.jpa.hibernate.ddl-auto=update`
- Thêm Springdoc config (`springdoc.api-docs.path=/v3/api-docs/user-service`)

---

### 3. API Gateway

---

#### [MODIFY] [pom.xml](file:///d:/workspace/EcoMove/eco-move-backend/infrastructure/api-gateway/pom.xml)

Thêm dependency Springdoc cho Gateway WebFlux:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-gateway-webflux-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

#### [NEW] `api-gateway/src/main/java/com/ecomove/gateway/filter/AuthGatewayFilter.java`

Custom `GatewayFilterFactory` để:
- **Bypass** các path: `/v3/api-docs/**`, `/swagger-ui/**`, `/actuator/**`, `/api/v1/auth/**`
- **Validate JWT Token** (phần validate – placeholder, sẽ extend sau)
- Kết thừa `AbstractGatewayFilterFactory<Config>`

---

### 4. User Service

---

#### [MODIFY] [pom.xml](file:///d:/workspace/EcoMove/eco-move-backend/core-services/user-service/pom.xml)

- Bỏ comment `spring-boot-starter-data-jpa` và `postgresql`
- Thêm `springdoc-openapi-starter-webmvc-ui` (version 2.5.0)
- Thêm `common-dto`, `common-exception` (đã có)

#### [NEW] `user-service/.../entity/BaseEntity.java`

Abstract base entity với các field:
```
created_at, updated_at, created_by, updated_by, is_deleted
```
Dùng `@MappedSuperclass`, `@EntityListeners(AuditingEntityListener.class)`.

#### [NEW] `user-service/.../entity/User.java`

Entity `users` table với fields: `user_id (UUID PK)`, `full_name`, `avatar`, `email`, `kyc_status`, `rating` + extends `BaseEntity`.

#### [NEW] `user-service/.../config/JpaConfig.java`

Bật `@EnableJpaAuditing`.

---

### 5. Auth Service (Core – 2FA Login)

---

#### [MODIFY] [pom.xml](file:///d:/workspace/EcoMove/eco-move-backend/core-services/auth-service/pom.xml)

Thêm dependencies còn thiếu:
- `common-dto` (1.0-SNAPSHOT)
- `common-exception` (1.0-SNAPSHOT)
- `common-logging` (1.0-SNAPSHOT)
- `common-validation` (1.0-SNAPSHOT)

#### [NEW] `auth-service/.../entity/BaseEntity.java`

Giống với `user-service` – abstract base entity có audit fields.

#### [NEW] `auth-service/.../entity/Credential.java`

Entity `credentials` table:
```
id (UUID, PK)
user_id (UUID, Ref)
phone (String, Unique, Indexed)
password_hash (String, Bcrypt)
role (Enum: RIDER, DRIVER, ADMIN)
status (Enum: ACTIVE, LOCKED, BANNED)
+ BaseEntity fields
```

#### [NEW] `auth-service/.../enums/Role.java` & `Status.java`

Enum `Role { RIDER, DRIVER, ADMIN }` và `Status { ACTIVE, LOCKED, BANNED }`.

#### [NEW] `auth-service/.../repository/CredentialRepository.java`

```java
Optional<Credential> findByPhone(String phone);
```

#### [NEW] `auth-service/.../dto/request/LoginRequest.java`

```java
{ phone: String, password: String }
```
Validation với `@RequireField` (từ `common-validation`).

#### [NEW] `auth-service/.../dto/request/VerifyOtpRequest.java`

```java
{ session_id: String, otp: String }
```

#### [NEW] `auth-service/.../dto/response/LoginResponse.java`

```java
{ session_id: String }
```

#### [NEW] `auth-service/.../dto/response/TokenResponse.java`

```java
{ access_token: String, refresh_token: String }
```

#### [NEW] `auth-service/.../config/JwtProperties.java`

`@ConfigurationProperties(prefix = "jwt")` chứa `secret`, `accessTokenExpiration`, `refreshTokenExpiration`.

#### [NEW] `auth-service/.../config/JwtConfig.java`

Khởi tạo `JwtProperties` bean và `BCryptPasswordEncoder`.

#### [NEW] `auth-service/.../util/JwtUtils.java`

Sử dụng thư viện `jjwt` (đã có trong pom):
- `generateAccessToken(userId, role)` – TTL từ config
- `generateRefreshToken(userId)` – TTL 7 ngày
- `validateToken(token)`
- Payload chỉ gồm: `sub (user_id)`, `role`, `iat`, `exp` (không có PII)

#### [NEW] `auth-service/.../config/RedisConfig.java`

Cấu hình `RedisTemplate<String, String>` với `StringRedisSerializer`.

#### [NEW] `auth-service/.../config/KafkaConfig.java`

Cấu hình `KafkaTemplate<String, String>` producer.

#### [NEW] `auth-service/.../event/SmsOtpEvent.java`

DTO event: `{ phone: String, otp: String }`.

#### [NEW] `auth-service/.../service/AuthService.java` (interface)

```java
LoginResponse login(LoginRequest request);
TokenResponse verifyOtp(VerifyOtpRequest request);
```

#### [NEW] `auth-service/.../service/impl/AuthServiceImpl.java`

Implement đầy đủ business logic theo spec:

**`login()`:**
1. `findByPhone(phone)` → throw `HttpException(401)` nếu không tìm thấy
2. `BCrypt.matches(password, hash)` → throw `HttpException(401)` nếu sai
3. Check `status == ACTIVE` → throw `HttpException(403)` nếu LOCKED/BANNED
4. Tạo `otp_code` (6 chữ số random)
5. Tạo `session_id` (UUID)
6. Redis.set(`OTP:{session_id}`, otp, TTL 5 phút)
7. Redis.set(`ATTEMPTS:{session_id}`, 0, TTL 5 phút)
8. **Lưu mapping:** Redis.set(`SESSION_USER:{session_id}`, credential.phone, TTL 5 phút) ← giải quyết vấn đề "làm sao map session → user" trong verifyOtp
9. Kafka.send(`sms_otp_events`, `{phone, otp}`)
10. Return `{ session_id }`

**`verifyOtp()`:**
1. `attempts = Redis.get("ATTEMPTS:{session_id}")` → throw `HttpException(429)` nếu >= 5
2. `cached_otp = Redis.get("OTP:{session_id}")` → throw `HttpException(400)` nếu null (expired)
3. `input_otp != cached_otp` → `Redis.increment("ATTEMPTS:{session_id}")` + throw `HttpException(401)`
4. `Redis.delete("OTP:{session_id}")`, `Redis.delete("ATTEMPTS:{session_id}")`
5. `phone = Redis.get("SESSION_USER:{session_id}")` → `credential = findByPhone(phone)`
6. `Redis.delete("SESSION_USER:{session_id}")`
7. Generate `access_token` + `refresh_token` bằng `JwtUtils`
8. Redis.set(`RT:{user_id}`, hash(refresh_token), TTL 7 ngày)
9. Return `{ access_token, refresh_token }`

#### [NEW] `auth-service/.../controller/AuthController.java`

```
POST /api/v1/auth/login       → authService.login(request)
POST /api/v1/auth/verify-otp  → authService.verifyOtp(request)
```
Swagger annotations với `@Tag`, `@Operation`.

#### [NEW] `auth-service/.../config/SecurityConfig.java`

Permit all `/api/v1/auth/**` (tắt Spring Security form login, disable CSRF), vì auth service là public endpoint.

#### [NEW] `auth-service/.../config/JpaConfig.java`

Bật `@EnableJpaAuditing`.

#### [NEW] Flyway migration: `auth-service/.../resources/db/migration/V1__create_credentials_table.sql`

```sql
CREATE TABLE credentials (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  phone VARCHAR(15) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP WITH TIME ZONE,
  updated_at TIMESTAMP WITH TIME ZONE,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_credentials_phone ON credentials(phone);
```

---

## Verification Plan

### Manual Verification

1. **Docker Compose** – Chạy `docker-compose up -d`, kiểm tra PostgreSQL tạo đúng `user_db` và `auth_db`
2. **Khởi động services** theo thứ tự: Discovery Server → Config Server → Auth Service → User Service → API Gateway
3. **Swagger UI** – Truy cập `http://localhost:8080/swagger-ui.html`, verify thấy 2 groups: `User Service` và `Auth Service`
4. **Login API** – `POST /api/v1/auth/login` với phone/password hợp lệ → nhận `session_id`
5. **Verify OTP** – `POST /api/v1/auth/verify-otp` với `session_id` + OTP (lấy từ Redis hoặc Kafka log) → nhận JWT tokens
6. **Brute-force protection** – Gọi verify-otp sai 5 lần → nhận 429

### Automated Verification
- Build: `mvn clean install -pl core-services/auth-service` (skip test nếu cần DB)
