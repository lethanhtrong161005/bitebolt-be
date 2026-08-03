# BiteBolt Backend Development Guidelines for Agents

Welcome Agent! This document defines the enterprise architecture rules, coding standards, and conventions that you MUST follow when writing code for the BiteBolt Backend project.

---

## 🏛️ Project Architecture Overview

BiteBolt is structured as a Maven multi-module monorepo containing:
- **`common-libraries`**: Shared packages built to be reused across different microservices.
  - `common-dto`: Unified API response formats, messages, DTO utilities, and global constants.
  - `common-validation`: Reusable validation annotations (e.g., `@RequireField`).
  - `common-exception`: Global exception handling configurations and wrapper HTTP exceptions.
  - `common-logging`: Shared MDC tracing interceptors.
  - `common-grpc`: Shared gRPC stubs, interceptors, and tracing metadata keys.
  - `common-security`: Centralized JWT generation/validation (JwtProvider) and Cryptography utilities.
- **`core-services`**: Independent business microservices (e.g., `auth-service`, `user-service`).
  - Microservices must interact with each other using **gRPC** for internal communication.
  - Direct HTTP calls between internal services are discouraged.

### 📚 Business Design Documents

The following documents define business rules that MUST be followed strictly:

| Document | Khi nào cần đọc |
|---|---|
| **[AUTH_BUSINESS_DESIGN.md](file:///d:/workspace/BiteBolt/bitebolt-backend/.agents/AUTH_BUSINESS_DESIGN.md)** | Trước khi implement tính năng login, xác thực, phân quyền |
| **[user-management-plan.md](file:///d:/workspace/BiteBolt/bitebolt-backend/.agents/user-management-plan.md)** | Khi làm việc với `user-service` (CRUD, Soft Delete) |

> **CRITICAL**: `auth-service` có 4 vai trò (`ADMIN`, `STAFF`, `SHIPPER`, `CUSTOMER`). `ADMIN`/`STAFF` dùng Microsoft Entra ID SSO. `SHIPPER`/`CUSTOMER` dùng Phone+OTP hoặc Google Social Login.

---

## ✉️ Rule 1: Enterprise Message Code Design (`messageCode`)

All exceptions (`HttpException`), error payloads, and success codes returned by endpoints must use standardized message keys. These keys are translated dynamically by the localization engine via resource bundles (`messages.properties`).

### 1. Global Message Codes (System-wide Common Codes)
- **Location**: Defined exclusively in `common-dto` under **[MessageConstant.java](file:///d:/workspace/BiteBolt/bitebolt-backend/common-libraries/common-dto/src/main/java/com/bitebolt/common/constant/MessageConstant.java)**.

### 2. Domain-Specific Message Codes
- **Location**: Defined **locally** in a service-specific constant file inside that service (e.g., `AuthMessageConstant.java` in `auth-service`).
- **Examples**: `ERROR_OTP_EXPIRED`, `ERROR_USER_NOT_FOUND`.

### 3. Localization Javadoc Documentation Requirements
Every constant representing a message key MUST contain a Javadoc comment summarizing its translations.
  ```java
  /**
   * VI: Mã OTP đã hết hạn hoặc phiên không hợp lệ.
   * EN: OTP code has expired or session is invalid.
   */
  public static final String ERROR_OTP_EXPIRED = "ERROR_OTP_EXPIRED";
  ```

---

## ⚙️ Rule 2: App Constants & Configs

- **Cross-Cutting App Constants**: Defined in `common-dto` under **[AppConstant.java](file:///d:/workspace/BiteBolt/bitebolt-backend/common-libraries/common-dto/src/main/java/com/bitebolt/common/constant/AppConstant.java)**.
- **Local Configuration Constants**: Defined **locally** in a specific constant file inside the owner service.

---

## 🪵 Rule 3: Enterprise Observability & Logging Architecture

BiteBolt uses a centralized distributed tracing and logging architecture (OpenSearch, Fluent Bit, Grafana, Tempo, Kafka). You MUST follow these 4 strict logging rules:

1. **Trace Context Injection**: Bắt buộc phải inject trace context (`MDC.put("traceId", ...)`) trong các async flow hoặc Kafka listener. (Trừ khi OpenTelemetry Java Agent đã tự động làm việc này).
2. **Structured JSON Output**: Phải dùng `logstash-logback-encoder` để output JSON trong file cấu hình Logback (`logback-spring.xml`). Tuyệt đối không tự viết custom string formatter hoặc in log kiểu text thô (`PatternLayoutEncoder`).
3. **Audit Trails for State Changes**: Bất kỳ hành động thay đổi data quan trọng nào (Create/Update/Delete user, config thay đổi, duyệt đơn hàng) **ĐỀU PHẢI** gắn annotation `@Auditable(action = AuditAction.XXX)` vào method để tự động emit audit event về `audit-service`. KHÔNG dùng `log.info()` cho mục đích Audit.
4. **Data Privacy (Masking)**: CẤM log dữ liệu nhạy cảm (Password, Token, OTP, Full Credit Card). Khi cần thiết phải log, bắt buộc phải bọc qua tiện ích `MaskingUtil.mask...()` thuộc `common-logging`.

## 🛑 Rule 4: Data Access & Spring Data JPA

### Soft Delete using `isDeleted`
- Models using Soft Delete must have a `Boolean isDeleted` field.
- **CRITICAL GOTCHA**: Do NOT use Spring Data method name derivation for checking `isDeleted` (e.g., `findAllByIsDeletedFalse`). This causes parsing errors in some Spring Data versions.
- **ALWAYS** use `@Query` with JPQL for filtering deleted records.
  ```java
  @Query("SELECT u FROM User u WHERE u.isDeleted = false")
  Page<User> findAllActiveUsers(Pageable pageable);
  ```

### Swagger UI & Pageable
- When using `Pageable` in `@GetMapping`, ALWAYS annotate it with `@ParameterObject` (from `org.springdoc.core.annotations.ParameterObject`).
- If you don't do this, Swagger UI will incorrectly render the Pageable parameter as a JSON body instead of query string parameters.
