# HƯỚNG DẪN THIẾT LẬP SOURCE CODE BACKEND CHUẨN DOANH NGHIỆP

**Dự án:** Eco-Move
[cite_start]**Kiến trúc:** Microservices[cite: 209]
**Công nghệ lõi:** Java 21, Spring Boot 4.xx, [cite_start]PostgreSQL, Kafka, Redis[cite: 209, 219, 237]

---

## 1. TỔNG QUAN CẤU TRÚC REPOSITORY (MULTI-MODULE)

[cite_start]Dự án được quản lý dưới dạng **Mono-repo Multi-module** bằng Maven hoặc Gradle[cite: 273]. [cite_start]Cấu trúc này giúp dễ dàng quản lý version, chia sẻ các thư viện chung (common) và CI/CD đồng bộ[cite: 273].

```text
eco-move-backend/
├── pom.xml (Parent POM)
├── common-libraries/            # Chứa code dùng chung cho toàn hệ thống
│   ├── common-dto/              # Chứa các Data Transfer Object chuẩn
│   ├── common-exception/        # Định nghĩa Global Exception và Error Response chuẩn
│   ├── common-security/         # Xử lý JWT Token và phân quyền cơ bản
│   └── common-logging/          # Cấu hình log, filter tự động gắn Trace ID
├── core-services/               # Chứa các Microservices nghiệp vụ
│   ├── user-service/            # Quản lý người dùng, driver
│   ├── trip-service/            # Quản lý vòng đời cuốc xe
│   ├── location-service/        # Xử lý toạ độ thời gian thực
│   └── payment-service/         # Xử lý thanh toán
├── infrastructure/              # Hạ tầng và cấu hình hệ thống
│   ├── api-gateway/             # Spring Cloud Gateway
│   ├── discovery-server/        # Eureka Server (nếu không dùng K8s Service)
│   ├── config-server/           # Quản lý cấu hình tập trung (Spring Cloud Config)
│   └── docker-compose.yml       # Dùng để dev ở local
└── k8s-manifests/               # Cấu hình triển khai Kubernetes


