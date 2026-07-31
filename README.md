# BiteBolt Backend Platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen)](https://spring.io/projects/spring-boot)
[![Java Version](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/technologies/downloads/)
[![gRPC](https://img.shields.io/badge/gRPC-Enabled-orange)](https://grpc.io/)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-Distributed-lightgrey)](https://kafka.apache.org/)

BiteBolt is an enterprise-grade, high-performance distributed food ordering platform. This repository contains the backend microservices monorepo structured as a Maven multi-module project.

---

## 🏛️ System Architecture

The platform is designed around **Domain-Driven Design (DDD)**, separating core domains into independent microservices communicating internally via high-performance **gRPC** and using **Apache Kafka** for event-driven message queuing.

```mermaid
graph TD
    Client[Mobile/Web Client]
    Gateway[API Gateway :8080]
    AuthService[Auth Service :8080/9090]
    UserService[User Service :8081/9091]
    Redis[(Redis Cache)]
    Postgres[(PostgreSQL)]
    Kafka{Kafka Event Bus}
    
    Client -->|HTTP Request| Gateway
    Gateway -->|Forward| AuthService
    AuthService -->|gRPC Call| UserService
    AuthService -->|Write Session/OTP| Redis
    AuthService -->|Publish Events| Kafka
    UserService -->|Read/Write Profile| Postgres
```

### Module Structure
* **`common-libraries/`**: Shared core packages.
  * `common-dto`: Reusable data transfer objects, base models, API response envelopes, and system constants.
  * `common-validation`: Custom annotation-driven validations (e.g. `@RequireField`).
  * `common-exception`: Global Exception Handling (`GlobalExceptionHandler`) and HTTP exceptions.
  * `common-logging`: Request logging interceptors and trace correlation utilities.
  * `common-grpc`: Centralized gRPC proto files, stub generation settings, and metadata interceptors.
  * `common-security`: Centralized JWT generation/validation (JwtProvider) and Cryptography utilities (CryptoUtils) shared across Gateway and Microservices.
* **`core-services/`**: Business microservices.
  * `auth-service`: Handle 2FA authentication, phone/password login, JWT generation, and Redis session states.
  * `user-service`: User profile repository, gRPC provider, and REST APIs for User Management CRUD.
* **`infrastructure/`**: Local deployment files, database seed scripts, and Docker compose files.
  * `api-gateway`: Spring Cloud Gateway handling centralized routing, JWT validation, Redis Blacklist checking, and standardized i18n error responses.
* **`.agents/`**: IDE instructions and coding style rules for AI development agents.

---

## 🔐 Authentication & Security Flow

> 📘 **Deep Dive**: Refer to the **[API Gateway Security Architecture](file:///d:/workspace/BiteBolt/bitebolt-backend/.agents/gateway-security/API_GATEWAY_SECURITY_PLAN.md)** for detailed technical implementation of our centralized security layer.

BiteBolt supports multiple client types (Web & Mobile) and enforces strict Role-Based Access Control (RBAC).

### Roles & Login Strategies
| Role | Strategy | Details |
|---|---|---|
| **ADMIN / STAFF** | SSO (Microsoft Entra ID) | Internal enterprise users log in exclusively via corporate SSO. |
| **SHIPPER / CUSTOMER** | Phone OTP / Google | End-users log in via 6-digit SMS OTP or Social Login. |

### Token Management & Blacklist
- **Dual Token Architecture**: Upon successful authentication, the system issues a short-lived `access_token` (JWT) and a long-lived `refresh_token`.
- **Client Adaptive Responses**: 
  - If `X-Client-Type: web` header is provided, tokens are securely set as `HttpOnly` cookies and Swagger UI testing must omit token body insertion to allow browser passthrough.
  - If omitted or set to `mobile` (default), tokens are returned in the JSON response body to be consumed via `Authorization: Bearer`.
- **Enterprise Logout & Blacklist**: 
  - Calling `POST /api/v1/auth/logout` deletes the active refresh token from Redis.
  - It also extracts the remaining TTL (Time-To-Live) of the access token, hashes it, and stores it in a **Redis Blacklist**. The Redis key automatically expires exactly when the JWT naturally expires, preventing memory bloat while ensuring immediate invalidation.

---

## 🛠️ Tech Stack & Prerequisites
- **Language**: Java 21
- **Framework**: Spring Boot 3.4.2 (Spring Cloud OpenFeign, Spring Security, Spring Data JPA)
- **Communications**: gRPC (protobuf-maven-plugin)
- **Streaming & Cache**: Apache Kafka, Redis (Spring Data Redis)
- **Database**: PostgreSQL (Flyway migration tool)
- **Containerization**: Docker Compose / Kubernetes

---

## 🚀 Getting Started

### 1. Clone & Set Up Local Infrastructure
Spin up the required local instances of Zookeeper, Apache Kafka, PostgreSQL, and Redis:
```bash
cd infrastructure
docker-compose up -d
```

### 2. Compile and Build Shared Libraries
Before running any microservice, you must compile and install the shared library packages to your local Maven repository:
```bash
cd common-libraries
mvn clean install
```

### 3. Running Microservices
Run the microservices using your IDE or via command line:

#### Run User Service:
```bash
cd core-services/user-service
../../mvnw spring-boot:run
```

#### Run Auth Service:
```bash
cd core-services/auth-service
../../mvnw spring-boot:run
```

---

## 🪵 Tracing & Logging Standards

BiteBolt uses a centralized distributed tracing design to trace requests across services:
1. Every client request generates or forwards an **`X-Trace-Id`** correlation header.
2. The `TraceIdInterceptor` binds this ID to the local SLF4J MDC (`traceId`).
3. When calling internal services, `GrpcTraceClientInterceptor` automatically propagates the metadata across the network.
4. The destination service extracts the trace ID and registers it locally, allowing complete, trace-correlated log streaming across all microservice boundaries.

---

## 📐 Coding Guidelines (For Developers & AI Agents)
Please refer to **[.agents/AGENTS.md](file:///d:/workspace/BiteBolt/bitebolt-backend/.agents/AGENTS.md)** for our strict enterprise coding rules:
- **Global Message Codes** (e.g., `ERROR_REQUIRED_FIELD`) are declared in `common-dto` (`MessageConstant`).
- **Domain-Specific Message Codes** (e.g., `ERROR_OTP_EXPIRED`) are declared locally inside each service (e.g., `AuthMessageConstant`).
- **Tracing Keys** are centralized globally in `AppConstant.java` while internal implementation variables (e.g. Redis key prefixes) are encapsulated locally.
