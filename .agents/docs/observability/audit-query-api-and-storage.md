# Backend Audit Query API & Storage Architecture

Hệ thống Kiểm toán (Audit Logging) của **BiteBolt Backend** tuân thủ theo tiêu chuẩn an ninh SOC2 / ISO 27001, đảm bảo toàn bộ hành vi người dùng và sự kiện hệ thống được ghi nhận đầy đủ, bất biến và dễ dàng tra cứu.

---

## 🏗️ Mô Hình Kiến Trúc Lưu Trữ Audit Log

```
[ Microservice / AOP @Auditable ] 
              │
              ▼ (Async JSON Event)
   [ Kafka Topic: audit.events ]
              │
              ▼ (AuditKafkaListener)
   [ audit-service (PostgreSQL audit_db) ]
              │
              ▼ (AuditQueryController GET /api/v1/audit/events)
   [ Admin Portal Frontend (bitebolt-fe) ]
```

1. **Ghi Nhận Audit Event**:
   - Sử dụng `@Auditable` tại Service Layer.
   - `AuditAspect` tự động bắt tham số, kết quả, IP client và `traceId`, gửi bất đồng bộ qua Kafka topic `audit.events`.
2. **Lưu Trữ Bất Biến**:
   - `audit-service` lắng nghe từ Kafka và ghi trực tiếp vào bảng `audit_logs` trong cơ sở dữ liệu `audit_db` (PostgreSQL).

---

## 📡 Chi Tiết API Query Audit Events

### Endpoint: `GET /api/v1/audit/events`

- **Mô tả**: API tìm kiếm và phân trang nhật ký kiểm toán dành cho Admin Portal.
- **Yêu cầu quyền**: Role `ADMIN`.
- **Query Parameters**:

| Tham Số | Kiểu Dữ Liệu | Mô Tả | Ví Dụ |
|---|---|---|---|
| `traceId` | `String` | Tìm kiếm chính xác theo Trace ID | `a3b8d1b6-0b3b-4b1a-9c1a-1a2b3c4d5e6f` |
| `actorId` | `String` | Tìm kiếm tương đối theo Email/User ID | `admin@bitebolt.com` |
| `action` | `String` | Lọc theo hành động (`AuditAction`) | `LOGIN_SUCCESS`, `SSO_LOGIN_SUCCESS`, `VERIFY_OTP` |
| `status` | `String` | Trạng thái thực thi | `SUCCESS` hoặc `FAILURE` |
| `startDate` | `ISO Date` | Thời gian bắt đầu | `2026-08-01T00:00:00Z` |
| `endDate` | `ISO Date` | Thời gian kết thúc | `2026-08-03T23:59:59Z` |
| `page` | `Integer` | Trang hiện tại (0-indexed, mặc định: 0) | `0` |
| `size` | `Integer` | Kích thước trang (mặc định: 20) | `20` |

### JSON Response Example:
```json
{
  "content": [
    {
      "id": 1042,
      "traceId": "c62b4852-5d9a-4c28-98e3-4f9e11223344",
      "actorId": "admin@bitebolt.com",
      "actorIp": "127.0.0.1",
      "action": "SSO_LOGIN_SUCCESS",
      "resourceType": "Credential",
      "resourceId": null,
      "status": "SUCCESS",
      "service": "auth-service",
      "details": "{\"email\":\"admin@bitebolt.com\",\"role\":\"ADMIN\"}",
      "createdAt": "2026-08-03T14:50:00Z"
    }
  ],
  "totalPages": 5,
  "totalElements": 98,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```
