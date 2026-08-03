# BiteBolt — Enterprise Logging Architecture Plan

## 1. Tổng quan

Dựa trên tài liệu **Logging-Architecture-Guide.md**, đây là thiết kế toàn diện cho hệ thống
observability của BiteBolt Microservices. Mục tiêu đạt chuẩn **ngân hàng & fintech** (NAB, Techcombank, VPBank).

---

## 2. Tech Stack được chọn

| Hạng mục | Công nghệ | Lý do |
|---|---|---|
| Logging API | **SLF4J** | Interface chuẩn Java, không vendor lock-in |
| Logging Impl | **Logback** | Default Spring Boot, hiệu suất cao, Async |
| JSON Encoder | **logstash-logback-encoder** | Output JSON native, tích hợp OpenTelemetry |
| Trace Context | **MDC** (đã có) + **OpenTelemetry** | Correlation xuyên service |
| Distributed Tracing | **OpenTelemetry Java Agent** | Chuẩn mới nhất, thay thế Sleuth |
| Trace Storage | **Tempo** (Grafana Stack) | Lightweight, tích hợp Grafana |
| Metrics | **Micrometer + Prometheus** | Chuẩn Spring Boot Actuator |
| Dashboard | **Grafana** | Unified: Logs + Metrics + Traces |
| Log Collector | **Fluent Bit** | Nhẹ hơn Logstash, chuẩn Kubernetes |
| Log Storage | **OpenSearch** | Open-source, tránh Elastic license |
| Log Dashboard | **OpenSearch Dashboards** | |
| Alert | **Alertmanager** | Tích hợp Prometheus |
| Audit Storage | **PostgreSQL** (audit-service DB riêng) | Append-only, compliant |

---

## 3. Kiến trúc tổng thể (Data Flow)

```
                    ┌─────────────────────────────┐
                    │     API Gateway              │
                    │  + TraceId inject (MDC)      │
                    │  + X-Client-IP inject        │
                    └──────────────┬──────────────┘
                                   │ forward request
              ┌────────────────────┼────────────────────┐
              ▼                    ▼                    ▼
       [auth-service]       [user-service]        [future-service]
              │                    │                    │
    ┌─────────▼──────────────────────────────────────────────┐
    │              common-logging library                     │
    │  RequestLog │ AuditLog │ SecurityLog │ PerformanceLog  │
    │  BusinessLog │ ExceptionLog │ SchedulerLog │ KafkaLog  │
    └─────────┬───────────────────────────┬───────────────────┘
              │ stdout (JSON)             │ Kafka (Audit)
              ▼                           ▼
         [Fluent Bit]           [kafka: audit.events]
              │                           │
              ▼                           ▼
        [OpenSearch]              [audit-service]
              │                           │
              ▼                     [PostgreSQL: audit_db]
    [OpenSearch Dashboards]
              │
              ▼
          [Grafana]  ◄──── [Prometheus] ◄─── [Micrometer]
              │
              ▼
        [Alertmanager] ──► Slack / Email / Teams
```

---

## 4. Phân loại Log (14 Categories)

### 4.1 Request Log
**Mô tả**: Log mọi HTTP request đến service.
**Nơi implement**: `common-logging` → `RequestLoggingFilter` (Servlet Filter)
```json
{
  "log_type": "REQUEST",
  "trace_id": "abc123",
  "method": "POST",
  "path": "/api/v1/auth/login",
  "client_ip": "192.168.1.1",
  "user_agent": "Mozilla/5.0",
  "actor_id": null
}
```

### 4.2 Response Log
**Mô tả**: Log HTTP response kèm status code và thời gian xử lý.
**Nơi implement**: `common-logging` → `ResponseLoggingFilter`
```json
{
  "log_type": "RESPONSE",
  "trace_id": "abc123",
  "status": 200,
  "duration_ms": 45
}
```

### 4.3 Business Log
**Mô tả**: Log nghiệp vụ chính. Dev chủ động `log.info()`.
**Nơi implement**: Từng service tự log trong Service layer.
```json
{
  "log_type": "BUSINESS",
  "trace_id": "abc123",
  "action": "OTP_SENT",
  "actor_id": "user-001",
  "message": "OTP sent to +84901234567"
}
```

### 4.4 Audit Log ⭐
**Mô tả**: Ghi nhận **ai làm gì với gì vào lúc nào**. Tamper-proof, append-only, lưu vào DB riêng.
**Nơi implement**: `common-logging` → `@Auditable` AOP + `AuditKafkaPublisher` → `audit-service`
```json
{
  "log_type": "AUDIT",
  "trace_id": "abc123",
  "actor_id": "user-001",
  "actor_ip": "192.168.1.1",
  "action": "LOGIN_SUCCESS",
  "resource_type": "Credential",
  "resource_id": "cred-xyz",
  "status": "SUCCESS",
  "service": "auth-service"
}
```

### 4.5 Security Log ⭐
**Mô tả**: Log các sự kiện bảo mật: token invalid, blacklist hit, access denied, brute-force.
**Nơi implement**: `common-logging` → `SecurityLogger` + `AuthGatewayFilterFactory`
```json
{
  "log_type": "SECURITY",
  "trace_id": "abc123",
  "event": "TOKEN_BLACKLISTED",
  "actor_ip": "192.168.1.1",
  "path": "/api/v1/user/profile",
  "severity": "HIGH"
}
```

### 4.6 Exception Log
**Mô tả**: Log toàn bộ exception với stack trace, phân biệt `expected` (4xx) vs `unexpected` (5xx).
**Nơi implement**: `common-logging` → `GlobalExceptionLogger` (Global `@ControllerAdvice`)
```json
{
  "log_type": "EXCEPTION",
  "trace_id": "abc123",
  "exception_class": "HttpException",
  "http_status": 401,
  "message": "Unauthorized",
  "is_expected": true
}
```

### 4.7 Performance Log
**Mô tả**: Log thời gian thực thi của các operation quan trọng (DB, gRPC, External API).
**Nơi implement**: `common-logging` → `@Timed` AOP hoặc Micrometer Timer
```json
{
  "log_type": "PERFORMANCE",
  "trace_id": "abc123",
  "operation": "grpc.getUserProfile",
  "duration_ms": 120,
  "threshold_ms": 100,
  "is_slow": true
}
```

### 4.8 Database Log
**Mô tả**: Log slow queries, transaction events. Không log toàn bộ SQL production.
**Nơi implement**: `application.properties` Hibernate SQL + P6Spy/Datasource-proxy (chỉ DEV)
```json
{
  "log_type": "DATABASE",
  "trace_id": "abc123",
  "query_type": "SLOW_QUERY",
  "duration_ms": 500,
  "table": "credentials"
}
```

### 4.9 Integration Log
**Mô tả**: Log tương tác với hệ thống bên ngoài: gRPC, REST external, SMS gateway.
**Nơi implement**: `common-logging` → `IntegrationLogger` dùng trong các `Client` class.
```json
{
  "log_type": "INTEGRATION",
  "trace_id": "abc123",
  "protocol": "gRPC",
  "target_service": "user-service",
  "method": "getUserProfile",
  "duration_ms": 30,
  "status": "OK"
}
```

### 4.10 Scheduler Log
**Mô tả**: Log job định kỳ: start, end, số record xử lý, lỗi.
**Nơi implement**: `common-logging` → `SchedulerLogger` dùng trong các `@Scheduled` method.
```json
{
  "log_type": "SCHEDULER",
  "job_name": "CleanExpiredOtpJob",
  "status": "COMPLETED",
  "records_processed": 42,
  "duration_ms": 200
}
```

### 4.11 Kafka Log
**Mô tả**: Log produce/consume message Kafka: topic, offset, lag, lỗi.
**Nơi implement**: `common-logging` → `KafkaLogger` wrap around `KafkaProducerHelper`.
```json
{
  "log_type": "KAFKA",
  "trace_id": "abc123",
  "direction": "PRODUCE",
  "topic": "sms_otp_events",
  "key": "+84901234567",
  "partition": 1,
  "offset": 1024,
  "status": "SUCCESS"
}
```

### 4.12 Redis Log
**Mô tả**: Log thao tác Redis quan trọng: cache miss, blacklist hit, TTL set.
**Nơi implement**: Service layer dùng `RedisLogger` utility (không log toàn bộ get/set).
```json
{
  "log_type": "REDIS",
  "trace_id": "abc123",
  "operation": "CACHE_MISS",
  "key_pattern": "otp:*",
  "note": "OTP session expired"
}
```

### 4.13 Startup Log
**Mô tả**: Log khi ứng dụng khởi động: config loaded, connections established, beans ready.
**Nơi implement**: `common-logging` → `StartupLogger` implement `ApplicationListener<ApplicationReadyEvent>`
```json
{
  "log_type": "STARTUP",
  "service": "auth-service",
  "version": "0.0.1-SNAPSHOT",
  "profile": "dev",
  "status": "READY",
  "startup_time_ms": 4200
}
```

### 4.14 Health Log
**Mô tả**: Log định kỳ trạng thái health check: DB, Redis, Kafka, gRPC endpoints.
**Nơi implement**: Spring Actuator Health + `HealthLogger` ghi log khi có thay đổi trạng thái.
```json
{
  "log_type": "HEALTH",
  "service": "auth-service",
  "component": "redis",
  "status": "DOWN",
  "previous_status": "UP"
}
```

---

## 5. Thiết kế `common-logging` Library

### Cấu trúc module sau khi refactor

```
common-logging/
├── pom.xml                          (thêm kafka, otel, micrometer)
└── src/main/
    ├── java/com/bitebolt/common/logging/
    │   ├── config/
    │   │   └── LoggingAutoConfig.java        # @AutoConfiguration entry point
    │   │
    │   ├── filter/
    │   │   ├── RequestResponseLoggingFilter.java  # REQUEST + RESPONSE log
    │   │   └── TraceIdFilter.java                 # MDC + OTel trace inject (đã có)
    │   │
    │   ├── audit/
    │   │   ├── Auditable.java                # @Annotation cho method
    │   │   ├── AuditAction.java              # Enum tất cả actions
    │   │   ├── AuditStatus.java              # SUCCESS | FAILURE
    │   │   ├── AuditEvent.java               # DTO gửi qua Kafka
    │   │   ├── AuditAspect.java              # @Around AOP interceptor
    │   │   └── AuditKafkaPublisher.java      # Fire-and-forget Kafka publish
    │   │
    │   ├── logger/
    │   │   ├── SecurityLogger.java           # SECURITY log helper
    │   │   ├── IntegrationLogger.java        # INTEGRATION log helper
    │   │   ├── KafkaLogger.java              # KAFKA produce/consume log
    │   │   ├── RedisLogger.java              # REDIS operation log
    │   │   ├── PerformanceLogger.java        # PERFORMANCE slow log helper
    │   │   ├── SchedulerLogger.java          # SCHEDULER job log helper
    │   │   └── StartupLogger.java            # STARTUP event log
    │   │
    │   ├── masking/
    │   │   └── MaskingUtil.java              # Mask phone, email, token
    │   │
    │   └── constant/
    │       └── LoggingConstants.java         # "log_type", field names
    │
    └── resources/
        ├── logback-spring.xml               # JSON encoder + async appender (cập nhật)
        └── META-INF/spring/
            └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## 6. Structured Log Schema — JSON Fields chuẩn

Mọi log JSON đều có **mandatory fields**:

```json
{
  "timestamp": "2026-07-31T09:15:30.123Z",
  "level": "INFO",
  "log_type": "AUDIT",
  "service": "auth-service",
  "trace_id": "abc123-...",
  "span_id": "def456-...",
  "thread": "http-nio-8082-exec-1",
  "class": "c.b.auth.service.impl.AuthServiceImpl",
  "message": "Login success for user",
  "actor_id": "user-001",
  "actor_ip": "192.168.1.1",
  "env": "dev"
}
```

**Naming Convention:**
- `snake_case` cho tất cả field names
- `log_type` luôn UPPERCASE: `REQUEST`, `AUDIT`, `SECURITY`, ...
- `trace_id` / `span_id` từ MDC (tích hợp OpenTelemetry)
- **KHÔNG BAO GIỜ** log: `password`, `otp`, `token` nguyên bản → dùng `MaskingUtil`

---

## 7. Logback Configuration (Nâng cấp)

**Cập nhật `logback-spring.xml`:**
```xml
<!-- Sử dụng logstash-logback-encoder thay PatternLayoutEncoder -->
<appender name="JSON_ASYNC" class="ch.qos.logback.classic.AsyncAppender">
  <appender-ref ref="JSON_CONSOLE" />
  <queueSize>512</queueSize>
  <discardingThreshold>0</discardingThreshold>  <!-- Không discard WARN/ERROR -->
</appender>

<appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
  <encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <fieldNames>
      <timestamp>timestamp</timestamp>
      <version>[ignore]</version>
    </fieldNames>
    <customFields>{"service":"${spring.application.name}"}</customFields>
  </encoder>
</appender>
```

---

## 8. Kafka Topic Design (Audit)

| Topic | Partitions | Retention | Consumer |
|---|---|---|---|
| `audit.events` | 3 | 30 ngày | `audit-service` |
| `security.events` | 3 | 90 ngày | `audit-service` (future SIEM) |

**Message Key**: `traceId` — đảm bảo ordering trong partition.

---

## 9. Infrastructure mới (docker-compose.dev.yml)

Thêm các service sau vào Docker Compose:

```yaml
# OpenSearch (thay Elasticsearch)
opensearch:
  image: opensearchproject/opensearch:2.11.0
  ports: ["9200:9200"]

# OpenSearch Dashboards
opensearch-dashboards:
  image: opensearchproject/opensearch-dashboards:2.11.0
  ports: ["5601:5601"]

# Fluent Bit (thu thập log từ stdout)
fluent-bit:
  image: fluent/fluent-bit:3.0
  volumes:
    - ./fluent-bit.conf:/fluent-bit/etc/fluent-bit.conf

# Prometheus
prometheus:
  image: prom/prometheus:v2.51.0
  ports: ["9090:9090"]

# Grafana
grafana:
  image: grafana/grafana:10.4.0
  ports: ["3001:3000"]

# Grafana Tempo (Distributed Tracing)
tempo:
  image: grafana/tempo:2.4.0
  ports: ["3200:3200", "4317:4317"]  # 4317: OTel gRPC receiver

# Alertmanager
alertmanager:
  image: prom/alertmanager:v0.27.0
  ports: ["9093:9093"]
```

---

## 10. OpenTelemetry Integration

Không cần code thêm trong service. Dùng **Java Agent**:

```bash
# Thêm vào VM Options khi chạy service
-javaagent:opentelemetry-javaagent.jar
-Dotel.service.name=auth-service
-Dotel.exporter.otlp.endpoint=http://localhost:4317
-Dotel.logs.exporter=none  # Log qua Fluent Bit, không OTel
```

OTel Agent tự động:
- Inject `traceId`, `spanId` vào MDC → xuất ra JSON log
- Tạo spans cho HTTP, JDBC, gRPC, Kafka tự động (zero-code)
- Export traces sang Tempo

---

## 11. Phạm vi thay đổi từng file

---

### Component 1: `common-libraries/common-logging`

#### [MODIFY] [pom.xml](file:///d:/workspace/bitebolt/bitebolt-be/common-libraries/common-logging/pom.xml)
- Thêm `logstash-logback-encoder`
- Thêm `spring-kafka`
- Thêm `spring-boot-starter-aop`
- Thêm `spring-boot-starter-actuator`
- Thêm `micrometer-registry-prometheus`

#### [MODIFY] [logback-spring.xml](file:///d:/workspace/bitebolt/bitebolt-be/common-libraries/common-logging/src/main/resources/logback-spring.xml)
- Đổi sang `LogstashEncoder` (JSON chuẩn)
- Bọc trong `AsyncAppender` (không block business thread)
- Thêm field `service`, `env`

#### [MODIFY] [TraceIdInterceptor.java](file:///d:/workspace/bitebolt/bitebolt-be/common-libraries/common-logging/src/main/java/com/bitebolt/common/logging/TraceIdInterceptor.java) → Đổi thành `TraceIdFilter.java`
- Đổi từ `HandlerInterceptor` sang `OncePerRequestFilter` (hoạt động ở Servlet level, sớm hơn)
- Thêm đọc `X-Client-IP` từ Gateway header vào MDC
- Thêm `actor_id` vào MDC sau khi resolve từ `SecurityContext`

#### [NEW] `filter/RequestResponseLoggingFilter.java`
- Log REQUEST khi request vào (method, path, IP)
- Log RESPONSE khi response ra (status, duration_ms)
- **Mask** request body nếu chứa field nhạy cảm

#### [NEW] `audit/` (4 files: `Auditable`, `AuditAction`, `AuditEvent`, `AuditAspect`, `AuditKafkaPublisher`)
- `AuditAction`: enum 30+ actions (LOGIN_SUCCESS, LOGOUT, SSO_*, USER_CREATED, ...)
- `AuditAspect`: `@Around` intercept → build `AuditEvent` → publish Kafka async

#### [NEW] `logger/SecurityLogger.java`
- Static helper: `SecurityLogger.tokenBlacklisted(ip, path)`, `SecurityLogger.accessDenied(...)`

#### [NEW] `logger/IntegrationLogger.java`
- Static helper: `IntegrationLogger.grpcCall(service, method, duration, status)`

#### [NEW] `logger/KafkaLogger.java`
- Wrap `KafkaProducerHelper` để log produce event tự động

#### [NEW] `masking/MaskingUtil.java`
```java
MaskingUtil.maskPhone("+84901234567") // → "+849012***67"
MaskingUtil.maskEmail("user@example.com") // → "us***@example.com"
MaskingUtil.maskToken("eyJhbGci...") // → "eyJhbGci...[MASKED]"
```

#### [NEW] `config/LoggingAutoConfig.java`
- `@AutoConfiguration` để tất cả service chỉ cần thêm dependency là có đủ

---

### Component 2: `infrastructure/audit-service` (Service mới)

#### [NEW] `audit-service/` — Spring Boot service
- Kafka Consumer: `@KafkaListener(topics = {"audit.events", "security.events"})`
- Repository: `AuditLogRepository` (JPA, PostgreSQL)
- Entity: `AuditLog` với `@Immutable` (append-only)
- REST API: `GET /api/v1/audit/logs` (phân trang, filter theo actorId, action, service, time range)
- Database: PostgreSQL DB riêng `audit_db` với full-text index trên `metadata` JSONB

---

### Component 3: `infrastructure/api-gateway`

#### [MODIFY] [AuthGatewayFilterFactory.java](file:///d:/workspace/bitebolt/bitebolt-be/infrastructure/api-gateway/src/main/java/com/bitebolt/gateway/filter/AuthGatewayFilterFactory.java)
- Inject `X-Client-IP` header (lấy từ `X-Forwarded-For` hoặc remote address)
- Inject `X-Trace-Id` header cho downstream services

---

### Component 4: `core-services/auth-service`

#### [MODIFY] [AuthServiceImpl.java](file:///d:/workspace/bitebolt/bitebolt-be/core-services/auth-service/src/main/java/com/bitebolt/auth/service/impl/AuthServiceImpl.java)
```java
@Auditable(action = AuditAction.LOGIN_SUCCESS, resourceType = "Credential")
public LoginResponse login(LoginRequest request) { ... }

@Auditable(action = AuditAction.LOGOUT)
public void logout(...) { ... }
```

#### [MODIFY] [EntraSsoServiceImpl.java](file:///d:/workspace/bitebolt/bitebolt-be/core-services/auth-service/src/main/java/com/bitebolt/auth/service/impl/EntraSsoServiceImpl.java)
```java
@Auditable(action = AuditAction.SSO_LOGIN_SUCCESS, resourceType = "Credential")
public String handleEntraCallback(...) { ... }
```

#### [MODIFY] `auth-service/pom.xml`
- Thêm `common-logging` dependency
- Thêm OTel Java Agent config

---

### Component 5: `infrastructure/docker-compose.dev.yml`

#### [MODIFY] [docker-compose.dev.yml](file:///d:/workspace/bitebolt/bitebolt-be/infrastructure/docker-compose.dev.yml)
- Thêm: `opensearch`, `opensearch-dashboards`, `fluent-bit`, `prometheus`, `grafana`, `tempo`, `alertmanager`

---

### Component 6: Documentation & Agent Guidelines

#### [MODIFY] [README.md](file:///d:/workspace/bitebolt/bitebolt-be/README.md)
- Bổ sung section **"Observability & Logging Architecture"**.
- Giải thích 14 loại log (Request, Audit, Security, v.v.).
- Hướng dẫn Developer cách sử dụng `@Auditable` và các Logger class (e.g., `SecurityLogger`).
- Nêu rõ rule: Không bao giờ dùng `log.info()` cho mục đích Audit hay Security (phải dùng thư viện chuẩn).
- Liệt kê bộ tech stack giám sát mới (OpenSearch, Fluent Bit, Grafana, Tempo).

#### [MODIFY] [.agents/AGENTS.md](file:///d:/workspace/bitebolt/bitebolt-be/.agents/AGENTS.md) (Hoặc tạo skill mới)
- Thêm rule cứng cho AI Agents: 
  - **Rule 1**: Bắt buộc phải inject trace context (`MDC.put("traceId", ...)` hoặc tự động qua OTel) trong các async flow hoặc Kafka listener.
  - **Rule 2**: Phải dùng `logstash-logback-encoder` để output JSON, tuyệt đối không dùng custom string formatter.
  - **Rule 3**: Bất kỳ hành động thay đổi data quan trọng (Create/Update/Delete user, config) đều phải gắp `@Auditable(action = ...)` vào method.
  - **Rule 4**: Cấm log dữ liệu nhạy cảm (Password, Token, OTP). Phải bọc qua `MaskingUtil.mask...()`.

---

## 12. Lộ trình triển khai (4 Phases)

### Phase 1 — Nền tảng (Ưu tiên cao nhất)
1. Nâng cấp `logback-spring.xml` → JSON chuẩn với `LogstashEncoder` + `AsyncAppender`
2. Viết `RequestResponseLoggingFilter` (REQUEST + RESPONSE log)
3. Upgrade `TraceIdFilter` → đọc `X-Client-IP` + `actor_id` vào MDC
4. Viết `MaskingUtil`
5. Viết `LoggingAutoConfig` (@AutoConfiguration)

### Phase 2 — Audit & Security Log
1. Viết `AuditAction` enum, `AuditEvent` DTO
2. Viết `AuditAspect` + `AuditKafkaPublisher`
3. Viết `SecurityLogger`
4. Tạo `audit-service`
5. Apply `@Auditable` vào `auth-service`

### Phase 3 — Integration & Performance Log
1. Viết `IntegrationLogger` → apply vào `UserGrpcClient`
2. Viết `KafkaLogger` → wrap `KafkaProducerHelper`
3. Viết `PerformanceLogger` → apply vào slow operation

### Phase 4 — Observability Stack
1. Thêm `opensearch` + `fluent-bit` vào docker-compose
2. Thêm `prometheus` + `grafana` + `tempo`
3. Cấu hình OTel Java Agent
4. Tạo Grafana dashboards: Request Rate, Error Rate, P99 Latency, Audit Events
5. Cập nhật `README.md` và `.agents/AGENTS.md` để document toàn bộ kiến trúc logging cho Human Developer và AI Agent.

---

## 13. Verification Plan

### Manual
1. Gọi `POST /api/v1/auth/login` → verify log JSON có đủ fields `trace_id`, `actor_ip`, `log_type: REQUEST`
2. Login thành công → verify `audit.events` topic trong Kafka UI có message `action: LOGIN_SUCCESS`
3. Dùng wrong token → verify log `log_type: SECURITY`, `event: TOKEN_INVALID`
4. Xem OpenSearch Dashboards → query theo `trace_id` thấy toàn bộ flow 1 request

### Grafana Dashboards (Phase 4)
- Request Rate per Service
- Error Rate (4xx/5xx)
- P50/P95/P99 Latency
- Audit Events by Action (Top 10)
- Security Events (Access Denied rate)
