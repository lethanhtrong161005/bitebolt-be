# Fix Plan: Swagger UI 403 Forbidden từ API Gateway

## Phân Tích Root Cause

### Luồng request hiện tại

```
Browser
  │
  ├─► GET /swagger-ui.html       → Gateway (serve local) ✅
  ├─► GET /v3/api-docs/swagger-config → Gateway (serve local) ✅
  ├─► GET /v3/api-docs/auth-service  → Route [3] → auth-service ❌ 403
  └─► GET /v3/api-docs/user-service  → Route [2] → user-service ❌ 403
```

---

### Root Cause 1 (CHÍNH): Anti-pattern – Non-standard path + Không có RewritePath

**Cấu hình hiện tại (sai):**
```properties
# auth-service.properties
springdoc.api-docs.path=/v3/api-docs/auth-service   ← non-standard path

# gateway.properties
routes[3].predicates[0]=Path=/v3/api-docs/auth-service
routes[3].uri=lb://auth-service
# Không có RewritePath → gateway forward nguyên path /v3/api-docs/auth-service
```

**Vấn đề:** Gateway forward path `/v3/api-docs/auth-service` xuống auth-service. Auth-service cũng serve tại path đó (khớp). NHƯNG Spring Security trong auth-service dùng `requestMatchers()` với Spring Boot 3.x, mặc định dùng **`MvcRequestMatcher`** (không phải `AntPathRequestMatcher`). `MvcRequestMatcher` kiểm tra pattern với Spring MVC handler mappings. Khi springdoc đăng ký endpoint tại path custom `/v3/api-docs/auth-service`, matcher có thể không khớp đúng với pattern `"/v3/api-docs/**"`.

**Pattern chuẩn (đúng):**
```properties
# auth-service: KHÔNG cấu hình springdoc.api-docs.path (dùng mặc định /v3/api-docs)

# gateway: Dùng SetPath để rewrite trước khi forward
routes[3].predicates[0]=Path=/v3/api-docs/auth-service
routes[3].uri=lb://auth-service
routes[3].filters[0]=SetPath=/v3/api-docs   ← rewrite về default path
```

---

### Root Cause 2: `WebSecurityCustomizer.web.ignoring()` Deprecated trong Spring Security 6

**Vấn đề:** `WebSecurityCustomizer` với `web.ignoring().requestMatchers()` bị deprecated trong Spring Security 6 (Spring Boot 3.x). Behavior thay đổi: khi Spring MVC có trên classpath, Spring Security ưu tiên dùng `MvcRequestMatcher` thay `AntPathRequestMatcher`, gây ra warring và có thể không hoạt động đúng.

**Fix:** Xóa `WebSecurityCustomizer`, chỉ dùng `permitAll()` trong `SecurityFilterChain`, và dùng `AntPathRequestMatcher` tường minh để bỏ qua path matching của MVC.

---

### Root Cause 3: Thiếu `server.forward-headers-strategy=framework`

**Vấn đề:** Khi auth-service và user-service chạy sau proxy (Gateway), springdoc generate server URL từ internal host/port (e.g., `localhost:8082`). Swagger UI nhận URL này và gửi request thẳng đến `localhost:8082` thay vì thông qua Gateway `localhost:8080`, bị block CORS hoặc không reach được.

**Fix:** Thêm `server.forward-headers-strategy=framework` vào từng service để springdoc dùng forwarded headers từ gateway.

---

### Root Cause 4: AuthController tại đường dẫn sai

**Vấn đề:** `AuthController.java` được tạo tại:
```
com\ecomove.auth\controller\AuthController.java  ← dấu chấm trong tên thư mục
```
Thay vì:
```
com\ecomove\auth\controller\AuthController.java  ← đúng
```
File khai báo `package com.ecomove.auth.controller;` nhưng vị trí vật lý không khớp, dẫn đến class có thể không được Spring load hoặc bị lỗi khi chạy.

---

## Proposed Changes

### Component 1: API Gateway (Config)

---

#### [MODIFY] [gateway.properties](file:///d:/workspace/EcoMove/eco-move-backend/eco-move-config/gateway.properties)

Thêm `SetPath` filter để rewrite path khi forward đến từng service's docs endpoint.

```diff
# Swagger Routing
 spring.cloud.gateway.routes[2].id=user-service-docs
 spring.cloud.gateway.routes[2].uri=lb://user-service
 spring.cloud.gateway.routes[2].predicates[0]=Path=/v3/api-docs/user-service
+spring.cloud.gateway.routes[2].filters[0]=SetPath=/v3/api-docs

 spring.cloud.gateway.routes[3].id=auth-service-docs
 spring.cloud.gateway.routes[3].uri=lb://auth-service
 spring.cloud.gateway.routes[3].predicates[0]=Path=/v3/api-docs/auth-service
+spring.cloud.gateway.routes[3].filters[0]=SetPath=/v3/api-docs
```

---

### Component 2: Config Server Properties

---

#### [MODIFY] [auth-service.properties](file:///d:/workspace/EcoMove/eco-move-backend/eco-move-config/auth-service.properties)

```diff
 # Cấu hình Swagger
-springdoc.api-docs.path=/v3/api-docs/auth-service
-springdoc.swagger-ui.path=/swagger-ui.html
+# Dùng default path /v3/api-docs (gateway sẽ rewrite khi route)
+springdoc.swagger-ui.enabled=false
+server.forward-headers-strategy=framework
```

> [!NOTE]
> Tắt Swagger UI trên downstream service vì Swagger UI chỉ cần chạy ở Gateway. Chỉ cần expose `/v3/api-docs` (JSON).

#### [MODIFY] [user-service.properties](file:///d:/workspace/EcoMove/eco-move-backend/eco-move-config/user-service.properties)

```diff
 # Cấu hình Swagger cho user-service
-springdoc.api-docs.path=/v3/api-docs/user-service
-springdoc.swagger-ui.path=/swagger-ui.html
+springdoc.swagger-ui.enabled=false
+server.forward-headers-strategy=framework
```

---

### Component 3: Auth Service – Security Config

---

#### [MODIFY] [SecurityConfig.java](file:///d:/workspace/EcoMove/eco-move-backend/core-services/auth-service/src/main/java/com/ecomove/auth/config/SecurityConfig.java)

Xóa deprecated `WebSecurityCustomizer`. Dùng `AntPathRequestMatcher` tường minh để bỏ qua path matching của Spring MVC:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
        "/api/v1/auth/**",
        "/v3/api-docs/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/actuator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_PATHS).permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

> [!IMPORTANT]
> Xóa hoàn toàn `WebSecurityCustomizer` bean vì deprecated và gây conflict trong Spring Security 6 với `MvcRequestMatcher`.

---

### Component 4: Auth Service – Fix AuthController path

---

#### [DELETE] `com\ecomove.auth\controller\AuthController.java` (wrong path với dot trong tên folder)

#### [NEW] [AuthController.java](file:///d:/workspace/EcoMove/eco-move-backend/core-services/auth-service/src/main/java/com/ecomove/auth/controller/AuthController.java) (đúng path, không có dot)

Tạo lại file tại đường dẫn chuẩn `com/ecomove/auth/controller/AuthController.java`.

---

### Component 5: Auth Service – ObjectMapper Bean

---

#### [NEW] `AppConfig.java` trong `com.ecomove.auth.config`

`AuthServiceImpl` inject `ObjectMapper` nhưng không có Bean nào define. Cần thêm:

```java
@Configuration
public class AppConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
```

---

## Verification Plan

### Flow kiểm tra sau khi fix

1. Restart **Config Server** → Restart **auth-service** → Restart **user-service** → Restart **api-gateway**

2. Truy cập `http://localhost:8080/swagger-ui.html` → Swagger UI load thành công

3. Chọn dropdown **"Auth Service"** → API docs của auth-service hiển thị

4. Chọn dropdown **"User Service"** → API docs của user-service hiển thị

5. Test trực tiếp từ Swagger UI: `POST /api/v1/auth/login`

### Kiểm tra cấu trúc sau khi fix

| Request | Gateway Route | Forwarded to | Expected |
|---|---|---|---|
| `/v3/api-docs/auth-service` | routes[3] | auth-service:8082`/v3/api-docs` | ✅ 200 JSON |
| `/v3/api-docs/user-service` | routes[2] | user-service:8081`/v3/api-docs` | ✅ 200 JSON |
| `/api/v1/auth/login` | routes[1] | auth-service:8082`/api/v1/auth/login` | ✅ 200 |
