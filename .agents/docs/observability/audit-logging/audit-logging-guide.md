# Hướng Dẫn Sử Dụng & Khởi Chạy Enterprise Audit Logging

Tài liệu này hướng dẫn chi tiết cách khởi chạy kiến trúc Audit Logging phân tán (dựa trên Kafka, MDC, và Spring AOP) và cách sử dụng các công cụ logging chuẩn doanh nghiệp trong dự án BiteBolt.

---

## 1. Tổng Quan Kiến Trúc (Architecture Overview)

BiteBolt sử dụng kiến trúc Logging tập trung gồm 3 thành phần chính:
1. **Trace Propagation**: Tự động sinh và truyền `traceId`, `client_ip`, `actor_id` qua MDC (Mapped Diagnostic Context) thông qua `TraceIdFilter`.
2. **Audit Trails (Kafka + AOP)**: Ghi nhận các hành động thay đổi trạng thái hệ thống quan trọng bằng annotation `@Auditable`. Dữ liệu được đẩy bất đồng bộ (async) qua Kafka topic `audit.events` và được `audit-service` lưu vào PostgreSQL.
3. **Structured JSON Logs**: Toàn bộ log console được format dưới dạng JSON thông qua `logstash-logback-encoder` để dễ dàng tích hợp với ELK/OpenSearch Stack.

---

## 2. Các Bước Khởi Chạy (Step-by-Step Startup)

Để hệ thống Audit hoạt động, bạn cần khởi chạy các dịch vụ theo đúng thứ tự sau:

### Bước 1: Start Infrastructure (Kafka, Postgres)
Mở terminal tại thư mục `infrastructure` và chạy Docker Compose để khởi động Kafka và Database:
```bash
cd infrastructure
docker-compose -f docker-compose.dev.yml up -d
```
*Lưu ý: Bạn phải đảm bảo file `.env.dev` ở thư mục gốc (bitebolt-be) đã được cấu hình đúng chuẩn.*

### Bước 2: Build các Common Libraries
Vì `audit-service` và `auth-service` đều phụ thuộc vào `common-logging`, bạn cần install library này vào Maven local:
```bash
cd common-libraries
./mvnw clean install
```
*(Trên Windows dùng `mvnw.cmd`)*

### Bước 3: Khởi chạy Config & Discovery
* **Discovery Server**: Chạy `discovery-server` ở port 8761.
* **Config Server**: Chạy `config-server` ở port 8888 (đảm bảo cấu hình `audit-service-dev.properties` đã được load thành công).

### Bước 4: Khởi chạy Core Services
* **API Gateway**: Start gateway (port 8080).
* **Audit Service**: Start `audit-service` (port 8083). Dịch vụ này sẽ tự động kết nối Kafka và tạo bảng `audit_logs` trong DB.
* **Auth Service** (hoặc User Service): Khởi chạy các service nghiệp vụ có sử dụng `@Auditable`.

---

## 3. Hướng Dẫn Sử Dụng (How to Use in Code)

### 3.1. Ghi nhận Audit Log (Thay đổi dữ liệu quan trọng)
Để tự động lưu lịch sử thay đổi (ví dụ: Tạo tài khoản, Đăng nhập, Duyệt đơn hàng), chỉ cần thêm annotation `@Auditable` vào method:

```java
import com.bitebolt.common.logging.audit.Auditable;
import com.bitebolt.common.logging.audit.AuditAction;

@Service
public class UserServiceImpl {

    @Auditable(action = AuditAction.USER_CREATED, resourceType = "User", resourceIdParam = "#userDto.email")
    public void createUser(UserDto userDto) {
        // Business logic...
    }
}
```
*Hệ thống sẽ tự động bắt Exception (nếu có) và đánh dấu status là `FAILURE` hoặc `SUCCESS`.*

### 3.2. Sử Dụng Các Logger Tiện Ích Chuyên Dụng
KHÔNG sử dụng `log.info` bừa bãi. Sử dụng các Helper Class trong `common-logging`:

* **Security Events** (Đăng nhập sai, hack, block token):
  ```java
  SecurityLogger.bruteForceDetected("/api/login", "5 failed attempts for user admin");
  ```
* **Integration Events** (Gọi API bên thứ 3 hoặc gRPC):
  ```java
  IntegrationLogger.logCall("HTTP", "Stripe", "chargeCard", 250, "SUCCESS");
  ```
* **Performance Monitoring** (Cảnh báo chạy chậm):
  ```java
  PerformanceLogger.logSlowOperation("generateReport", 3500, 2000); // Ngưỡng là 2000ms
  ```
* **Kafka Tracing** (Dành cho Producer/Consumer khác ngoài Audit):
  ```java
  KafkaLogger.logProduceSuccess("user.events", "user-123", 0, 15);
  ```

### 3.3. Che Giấu Dữ Liệu Nhạy Cảm (Data Privacy)
Trước khi log email, SĐT, hoặc token ra console, BẮT BUỘC dùng `MaskingUtil`:
```java
import com.bitebolt.common.logging.masking.MaskingUtil;

log.info("Processing OTP for user: {}", MaskingUtil.maskEmail(email)); 
// Output: an***@gmail.com
```

---

## 4. Cách Kiểm Tra (Verification)

1. Thực hiện một API call có gắn `@Auditable` (ví dụ: Gọi API Đăng nhập SSO).
2. **Kiểm tra Console Service Gốc**: Bạn sẽ thấy HTTP Request/Response được in ra dưới định dạng JSON có chứa `traceId`.
3. **Kiểm tra Console Audit Service**: Sẽ có dòng log: `Saved audit event: traceId=..., action=SSO_LOGIN_SUCCESS`.
4. **Kiểm tra Database**: Mở DBeaver/PgAdmin kết nối tới Postgres database `audit_db`, query bảng `audit_logs` để xem toàn bộ thông tin chi tiết (ai thực hiện, IP nào, trạng thái thành công hay thất bại).

---

## 5. Hướng Dẫn Kiểm Tra Log Bằng Hệ Thống Observability (Step-by-Step)

Hệ thống đã được tích hợp bộ công cụ Observability chuẩn doanh nghiệp thông qua Docker Compose. Dưới đây là cách kiểm tra:

### 5.1. Xem Log Tập Trung với OpenSearch Dashboards
Tất cả JSON Logs sinh ra từ các microservice sẽ được Fluent Bit tự động thu thập và đẩy vào OpenSearch.

1. **Truy cập Dashboards**: Mở trình duyệt và vào `http://localhost:5601`.
   - **Tài khoản**: `admin`
   - **Mật khẩu**: `StrongP@ssw0rd123` (Hoặc theo biến môi trường `OPENSEARCH_PASSWORD` trong file `.env.dev`)
2. **Thiết lập Data View (Lần đầu tiên)**:
   - Truy cập trực tiếp link này: `http://localhost:5601/app/management/opensearch-dashboards/dataViews`
   - Bấm nút **Create data view**.
   - Tại mục *Name* và *Index pattern*, bạn nhập chính xác chữ: `fluent-bit`
   - Tại mục *Time field*, chọn `@timestamp` từ menu thả xuống.
   - Bấm **Save data view** để lưu lại.
3. **Tra cứu Log bằng Trace ID**:
   - Truy cập trực tiếp trang **Discover**: `http://localhost:5601/app/discover`
   - Đảm bảo ở góc trái màn hình (dưới thanh tìm kiếm), Data view đang được chọn là `fluent-bit`.
   - Để tìm kiếm toàn bộ hành trình của một request, hãy copy ID từ trường `traceRequest` (trả về từ API) và nhập lệnh sau vào ô tìm kiếm:
     ```
     traceRequest: "d071d84f-4ce8-450f-939f-2c2a71ac7244"
     ```
   - Nhấn **Enter** hoặc nút **Update**. OpenSearch sẽ lọc ra chính xác tất cả các log của Gateway, Auth, Audit... liên quan tới request đó. Bấm mũi tên ở đầu mỗi dòng log để xem chi tiết.

### 5.2. Theo Dõi Metrics & Cảnh Báo với Grafana & Prometheus
Prometheus sẽ thu thập số liệu (CPU, RAM, số lượng log error) và Grafana dùng để trực quan hóa.

1. **Truy cập Grafana**: Vào `http://localhost:3001` (User: `admin` / Pass: `admin` - *mặc định, bạn có thể được yêu cầu đổi pass*).
2. **Thêm Data Source**:
   - Vào **Connections** -> **Data Sources** -> **Add data source** -> Chọn **Prometheus**.
   - URL: `http://prometheus:9090` -> **Save & Test**.
3. **Import Dashboard**:
   - Bạn có thể tạo Dashboard mới hoặc Import các template có sẵn (như Spring Boot 3.x / Micrometer Dashboard ID: `19004` hoặc `11378`) để theo dõi sức khỏe ứng dụng.

### 5.3. Distributed Tracing với Grafana Tempo
1. Tại Grafana (`http://localhost:3001`), thêm Data Source mới là **Tempo**.
2. URL cấu hình: `http://tempo:3200`.
3. Trong giao diện **Explore** của Grafana, chọn data source là Tempo.
4. Nhập `traceId` (đã copy từ OpenSearch hoặc Console) vào thanh tìm kiếm. Bạn sẽ nhìn thấy biểu đồ thác nước (Waterfall diagram) thể hiện thời gian gọi giữa Gateway -> Microservices -> Database, giúp dễ dàng phát hiện nút thắt cổ chai (bottleneck) về hiệu năng.

---

## 6. Cấu Hình Các Biến Môi Trường Hệ Thống

Để đảm bảo các Microservice của BiteBolt có thể giao tiếp chính xác với cụm Observability, hệ thống sử dụng 2 biến môi trường cốt lõi được định nghĩa trong file `.env.dev` và `.env.prod`:

1. **`LOGSTASH_DESTINATION`**: Chỉ định địa chỉ TCP để bộ thư viện `common-logging` (thông qua `LogstashTcpSocketAppender`) đẩy thẳng log chuẩn JSON sang cổng 24224 của Fluent Bit.
2. **`TEMPO_ENDPOINT`**: Cấu hình địa chỉ OTLP HTTP (cổng 4318) để OpenTelemetry tự động đóng gói các Spans và đẩy về cho Grafana Tempo vẽ biểu đồ thác nước.

**Môi trường Development (`.env.dev`)**
*(Dành cho lập trình viên chạy Microservice trực tiếp từ IntelliJ ở máy cá nhân)*
```properties
LOGSTASH_DESTINATION=localhost:24224
TEMPO_ENDPOINT=http://localhost:4318/v1/traces
```

**Môi trường Production (`.env.prod`)**
*(Dành cho các Microservice đóng gói và chạy bên trong mạng lưới Docker Compose / Kubernetes)*
```properties
LOGSTASH_DESTINATION=fluent-bit:24224
TEMPO_ENDPOINT=http://tempo:4318/v1/traces
```

> [!TIP]
> Việc tách biệt các biến này giúp bạn không bao giờ phải sửa file `logback-spring.xml` hay `application.properties` khi mang dự án lên triển khai thực tế. Mọi luồng dữ liệu tự động thay đổi bến đỗ nhờ cấu hình linh hoạt trong `.env`!
