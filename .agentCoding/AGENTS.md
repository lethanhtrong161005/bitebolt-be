# EcoMove Backend Development Guidelines for Agents

Welcome Agent! This document defines the enterprise architecture rules, coding standards, and conventions that you MUST follow when writing code for the EcoMove Backend project.

---

## 🏛️ Project Architecture Overview

EcoMove is structured as a Maven multi-module monorepo containing:
- **`common-libraries`**: Shared packages built to be reused across different microservices.
  - `common-dto`: Unified API response formats, messages, DTO utilities, and global constants.
  - `common-validation`: Reusable validation annotations (e.g., `@RequireField`).
  - `common-exception`: Global exception handling configurations and wrapper HTTP exceptions.
  - `common-logging`: Shared MDC tracing interceptors.
  - `common-grpc`: Shared gRPC stubs, interceptors, and tracing metadata keys.
- **`core-services`**: Independent business microservices (e.g., `auth-service`, `user-service`).
  - Microservices must interact with each other using **gRPC** for internal communication.
  - Direct HTTP calls between internal services are discouraged.

---

## ✉️ Rule 1: Enterprise Message Code Design (`messageCode`)

All exceptions (`HttpException`), error payloads, and success codes returned by endpoints must use standardized message keys. These keys are translated dynamically by the localization engine via resource bundles (`messages.properties`).

### 1. Global Message Codes (System-wide Common Codes)
- **Definition**: Message codes representing framework-level errors, generic validation, or standard HTTP issues.
- **Location**: Defined exclusively in `common-dto` under **[MessageConstant.java](file:///d:/workspace/EcoMove/eco-move-backend/common-libraries/common-dto/src/main/java/com/ecomove/common/constant/MessageConstant.java)**.
- **Examples**: `ERROR_REQUIRED_FIELD`, `ERROR_BAD_REQUEST`, `ERROR_INTERNAL_SERVER_ERROR`.

### 2. Domain-Specific Message Codes (Local Service Business Codes)
- **Definition**: Message codes representing business logic/domain exceptions unique to a single microservice.
- **Location**: Defined **locally** in a service-specific constant file inside that service (e.g., `AuthMessageConstant.java` under `com.ecomove.auth.constant` in `auth-service`).
- **Reasoning**: Storing service-specific keys in `common-dto` violates **Loose Coupling (Lỏng lẻo)** and **Encapsulation (Đóng gói)**. Modifying an auth error would force rebuilding/redeploying unrelated services like `user-service`.
- **Examples**: `ERROR_OTP_EXPIRED`, `ERROR_INVALID_OTP`, `ERROR_USER_NOT_FOUND`.

### 3. Localization Javadoc Documentation Requirements
Every hằng số (constant) representing a message key MUST contain a Javadoc comment summarizing its translations. This allows developers to see the Vietnamese and English messages directly when inspecting code in the IDE.
- **Example format**:
  ```java
  /**
   * VI: Mã OTP đã hết hạn hoặc phiên không hợp lệ.
   * EN: OTP code has expired or session is invalid.
   */
  public static final String ERROR_OTP_EXPIRED = "ERROR_OTP_EXPIRED";
  ```

---

## ⚙️ Rule 2: Enterprise App Constants & Configuration Design

We separate constants based on their scope and responsibilities to keep dependencies clean:

### 1. Cross-Cutting App Constants (Global System Concerns)
- **Definition**: Keys or values that propagate across service boundaries and must be referenced identically everywhere.
- **Location**: Defined in `common-dto` under **[AppConstant.java](file:///d:/workspace/EcoMove/eco-move-backend/common-libraries/common-dto/src/main/java/com/ecomove/common/constant/AppConstant.java)**.
- **Usage**: MDC keys (`traceId`), Request Header names (`X-Trace-Id`), and global HTTP metadata.

### 2. Local Configuration Constants (Service Implementation Details)
- **Definition**: Constants indicating local datastore configurations, database queries, or key prefixes.
- **Location**: Defined **locally** in a specific constant file inside the owner service (e.g., `AuthRedisConstant.java` under `com.ecomove.auth.constant` in `auth-service`).
- **Examples**: Redis key prefixes (`OTP:`, `ATTEMPTS:`, `RT:`).

---

## 🪵 Rule 3: Tracing & Logging Propagation Standards

We maintain a strict distributed tracing standard:
- Trace IDs (`traceId`) must propagate automatically across HTTP boundaries via `TraceIdInterceptor` and gRPC boundaries using custom metadata interceptors (`GrpcTraceClientInterceptor` / `GrpcTraceServerInterceptor`).
- Always map gRPC errors cleanly (e.g. `NOT_FOUND` to HTTP `401/404`, `DEADLINE_EXCEEDED` to HTTP `408 Request Timeout` / `ERROR_USER_SERVICE_TIMEOUT`).
- Clean up MDC context variables upon completion of processing threads to prevent logs pollution.
