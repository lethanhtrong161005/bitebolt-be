# Phase 2 — External Multi-Auth Plan
## DRIVER & RIDER via Phone+OTP and Google Social Login

> **Phase**: 2  
> **Phạm vi**: Xác thực bên ngoài cho `DRIVER` và `RIDER`  
> **Phương thức**: (1) Phone + Password + OTP *(đã có)* | (2) Google Social Login *(cần implement)*  
> **Tài liệu tham chiếu**: [AUTH_BUSINESS_DESIGN.md](file:///d:/workspace/BiteBolt/bitebolt-backend/.agents/AUTH_BUSINESS_DESIGN.md)

---

## 1. 🎯 Mục Tiêu

Phase 2 bao gồm hai nhánh công việc:

| Nhánh | Trạng thái | Mô tả |
|---|---|---|
| **Phone + Password + OTP** | ✅ Đã hoàn thành | Luồng 2FA hiện tại |
| **Google Social Login** | 🔲 Cần implement | Đăng nhập bằng Google OAuth 2.0 |

Trọng tâm của Phase 2 là implement **Google Social Login**, đồng thời tích hợp thêm guard đảm bảo `DRIVER`/`RIDER` không thể dùng SSO và `ADMIN`/`STAFF` không thể dùng Google Login.

---

## 2. 📊 Trạng Thái Hiện Tại (Phone+OTP — Đã Implement)

### Endpoints đang hoạt động:

```
POST /api/v1/auth/login
  Body: { "phone": "...", "password": "..." }
  → Gửi OTP qua Kafka → SMS Service
  → Trả về: { "session_id": "..." }

POST /api/v1/auth/verify-otp
  Body: { "session_id": "...", "otp": "..." }
  Header: X-Client-Type: web | mobile
  → Web: token qua HttpOnly Cookie
  → Mobile: token trong JSON body
```

### Files đã tồn tại:
- `AuthController.java` — endpoints login, verify-otp
- `AuthServiceImpl.java` — business logic OTP
- `JwtUtils.java` — generate/validate JWT
- `CookieUtils.java` — build HttpOnly cookies
- `AuthRedisConstant.java` — OTP/RT Redis key prefixes

> **Không được sửa** các file này khi implement Google SSO trừ khi thêm guard cần thiết.

---

## 3. 📦 Dependencies Cần Thêm vào `auth-service/pom.xml`

> **Lưu ý**: Dependency này có thể đã được thêm ở Phase 1. Kiểm tra `pom.xml` trước khi thêm lại.

```xml
<!-- Spring Security OAuth2 Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

---

## 4. 🗄️ Database Schema Changes

### 4.1. Prerequisite — Flyway Migration từ Phase 1

Phase 2 **phụ thuộc** vào migration `V2__add_auth_method_and_sso_fields.sql` đã được tạo ở Phase 1 (thêm `auth_method`, `email`, `entra_object_id`).

### 4.2. Migration bổ sung cho Google

File: `V3__add_google_social_fields.sql`

```sql
-- Thêm Google-specific columns
ALTER TABLE credentials
    ADD COLUMN google_sub VARCHAR(255) UNIQUE;

CREATE UNIQUE INDEX idx_credentials_google_sub ON credentials(google_sub) WHERE google_sub IS NOT NULL;
```

### 4.3. Cập nhật `Credential.java` entity — thêm field Google

```java
@Column(name = "google_sub", unique = true)
private String googleSub;  // Google User ID (immutable, ổn định hơn email)
```

---

## 5. ⚙️ Configuration Changes

### 5.1. Thêm `GoogleOAuthProperties.java` — `com.bitebolt.auth.config`

```java
@Data
@Component
@ConfigurationProperties(prefix = "google.oauth")
public class GoogleOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
```

### 5.2. Thêm vào `auth-service.properties`

```properties
# Google OAuth2 Social Login Config
google.oauth.client-id=${GOOGLE_OAUTH_CLIENT_ID}
google.oauth.client-secret=${GOOGLE_OAUTH_CLIENT_SECRET}
google.oauth.redirect-uri=http://localhost:8082/api/v1/auth/social/google/callback
```

### 5.3. Đăng ký Google OAuth App (Bước ngoài code — DevOps/IT)

1. Vào [Google Cloud Console](https://console.cloud.google.com) → **APIs & Services** → **Credentials**.
2. Tạo **OAuth 2.0 Client ID** → Application type: Web Application.
3. Thêm Authorized redirect URIs: `http://localhost:8082/api/v1/auth/social/google/callback`.
4. Lấy **Client ID** và **Client Secret**.
5. Enable **Google People API**.

---

## 6. 📁 Cấu Trúc File Mới Cần Tạo

```
com.bitebolt.auth
├── config/
│   └── GoogleOAuthProperties.java         [NEW]
├── constant/
│   └── AuthMessageConstant.java            [MODIFY] — thêm Google error codes
│   └── AuthRedisConstant.java              [MODIFY] — thêm Google state prefix
├── controller/
│   └── SocialAuthController.java           [NEW]
├── dto/
│   └── response/
│       └── SocialLoginResponse.java        [NEW] — redirect URL hoặc token info
├── enums/
│   └── AuthMethod.java                     [MODIFY nếu chưa tạo ở Phase 1]
├── entity/
│   └── Credential.java                     [MODIFY] — thêm googleSub
├── service/
│   ├── SocialAuthService.java              [NEW] — interface
│   └── impl/
│       └── GoogleSocialAuthServiceImpl.java [NEW]
└── repository/
    └── CredentialRepository.java           [MODIFY] — thêm findByGoogleSub
```

---

## 7. 🔌 API Endpoints Cần Implement

### `SocialAuthController.java`

```
GET  /api/v1/auth/social/google
     → Tạo Google Authorization URL và redirect.

GET  /api/v1/auth/social/google/callback?code=xxx&state=xxx
     → Nhận code, exchange token, validate, issue BiteBolt JWT.
```

**Luồng chi tiết — Google Social Login:**

```
[Mobile App / Web Browser]
    │ GET /api/v1/auth/social/google
    ▼
[SocialAuthController.initiateGoogleLogin()]
    │ Build Google Authorization URL:
    │   https://accounts.google.com/o/oauth2/v2/auth
    │   ?client_id={clientId}
    │   &response_type=code
    │   &redirect_uri={redirectUri}
    │   &scope=openid profile email
    │   &state={randomState}   ← lưu vào Redis (CSRF protection)
    │ Redirect 302 → Google Login Page
    ▼
[Google — User chọn tài khoản Google]
    │ Authorize → Redirect đến /api/v1/auth/social/google/callback?code=xxx&state=xxx
    ▼
[SocialAuthController.handleGoogleCallback()]
    │ 1. Validate state với Redis (chống CSRF)
    │ 2. Exchange code → Google Access Token + ID Token
    │ 3. Decode ID Token (hoặc call Google UserInfo endpoint)
    │    → extract: sub (User ID), email, name, picture
    │ 4. Kiểm tra role conflict:
    │    - Nếu email trùng với tài khoản ADMIN/STAFF → 403 ERROR_SSO_REQUIRED
    │ 5. Lookup Credential bằng googleSub
    │    - Nếu chưa có → auto-register mới với role RIDER (default)
    │      * Gọi gRPC sang user-service để tạo User Profile
    │      * Tạo Credential với authMethod=GOOGLE
    │    - Nếu đã có → đăng nhập bình thường
    │ 6. Kiểm tra tài khoản: status phải là ACTIVE
    │ 7. Kiểm tra role: chỉ DRIVER/RIDER mới được phép
    │ 8. Generate BiteBolt JWT:
    │    - claim: authMethod = "GOOGLE"
    │ 9. Trả về token theo X-Client-Type:
    │    - web: HttpOnly Cookie
    │    - mobile: JSON body
    ▼
[Client — đã đăng nhập thành công]
```

---

## 8. 🔗 Tích Hợp gRPC với `user-service` khi Auto-Register

Khi RIDER đăng nhập Google lần đầu, cần auto-tạo User Profile:

```
GoogleSocialAuthServiceImpl
    │
    │ 1. Tìm kiếm trong credentials bằng googleSub → không có
    │ 2. Gọi gRPC → user-service.createUserFromSocialLogin()
    │    Request: { email, fullName, avatar (Google picture URL) }
    │    Response: { userId (UUID) }
    │ 3. Tạo Credential mới:
    │    { userId, email, googleSub, authMethod=GOOGLE, role=RIDER, status=ACTIVE }
    │ 4. Save Credential
    │ 5. Generate JWT và trả về
```

> **gRPC method cần thêm vào `user-service`**: `CreateUserFromSocialLogin` — sẽ được định nghĩa trong `common-grpc` proto file.

---

## 9. 🛡️ Guards & Validation

### 9.1. Guard: DRIVER/RIDER không được dùng SSO endpoints

Thêm vào `EntraSsoServiceImpl.handleCallback()` (Phase 1):
```java
// Chỉ ADMIN/STAFF mới được dùng SSO — đây là safety check, không phải auto-detect
if (!INTERNAL_ROLES.contains(credential.getRole())) {
    throw new HttpException(403, AuthMessageConstant.ERROR_AUTH_METHOD_NOT_ALLOWED);
}
```

### 9.2. Guard: ADMIN/STAFF không được dùng Google Login

Thêm vào `GoogleSocialAuthServiceImpl` trước khi issue JWT:
```java
// Không cho phép email của internal account đăng nhập qua Google
credentialRepository.findByEmail(googleEmail).ifPresent(existing -> {
    if (INTERNAL_ROLES.contains(existing.getRole())) {
        throw new HttpException(403, AuthMessageConstant.ERROR_SSO_REQUIRED);
    }
});
```

---

## 10. 📨 Message Codes Cần Thêm vào `AuthMessageConstant.java`

```java
/**
 * VI: Phương thức đăng nhập này không được phép cho vai trò của bạn.
 * EN: This authentication method is not allowed for your role.
 */
public static final String ERROR_AUTH_METHOD_NOT_ALLOWED = "ERROR_AUTH_METHOD_NOT_ALLOWED";

/**
 * VI: Email này phải đăng nhập qua Microsoft SSO.
 * EN: This email must authenticate via Microsoft SSO.
 */
public static final String ERROR_SSO_REQUIRED = "ERROR_SSO_REQUIRED";  // (có thể đã thêm ở Phase 1)

/**
 * VI: Đăng nhập Google thành công.
 * EN: Google login successful.
 */
public static final String SUCCESS_GOOGLE_LOGIN = "SUCCESS_GOOGLE_LOGIN";

/**
 * VI: Đăng ký tài khoản mới thành công qua Google.
 * EN: New account registered successfully via Google.
 */
public static final String SUCCESS_GOOGLE_REGISTER = "SUCCESS_GOOGLE_REGISTER";
```

---

## 11. 🔑 Redis Keys Cần Thêm vào `AuthRedisConstant.java`

```java
/** Prefix lưu state Google OAuth để chống CSRF */
public static final String GOOGLE_STATE_PREFIX = "GOOGLE_STATE:";
// TTL: 10 phút
```

---

## 12. 🔄 Cập Nhật `SecurityConfig.java`

Thêm Google callback endpoint vào `PUBLIC_PATHS`:

```java
private static final String[] PUBLIC_PATHS = {
    "/api/v1/auth/**",          // login, verify-otp, sso/entra/**, social/**
    "/v3/api-docs/**",
    "/swagger-ui/**",
    "/swagger-ui.html",
    "/actuator/**",
    "/error"
};
```

> Vì đang dùng wildcard `/api/v1/auth/**`, không cần thay đổi gì thêm.

---

## 13. 📱 Lưu Ý Đặc Biệt cho Mobile App

Mobile App **không dùng browser redirect** để xử lý Google OAuth. Cần hai cách tiếp cận:

### Cách A — Google Sign-In SDK (Recommended for Mobile)
Mobile App dùng **Google Sign-In SDK** native để lấy `idToken`, sau đó gửi `idToken` lên backend:

```
POST /api/v1/auth/social/google/verify-id-token
Header: X-Client-Type: mobile
Body: { "id_token": "..." }
→ Backend verify idToken với Google → issue BiteBolt JWT
```

### Cách B — Web-based redirect (Fallback)
Mobile App mở WebView/browser để thực hiện redirect flow như web.

> **Khuyến nghị**: Implement cả hai endpoint. SDK native (`/verify-id-token`) cho app performance tốt hơn.

---

## 14. 🧪 Verification Plan

### Test Cases:

| Test Case | Input | Expected Output |
|---|---|---|
| Redirect Google | `GET /api/v1/auth/social/google` | `302` redirect đến Google |
| RIDER đăng nhập Google lần đầu | Email chưa có trong DB | Auto-register, `201`, role=RIDER, JWT |
| RIDER đã có tài khoản | Google email đã tồn tại | `200`, JWT |
| DRIVER đăng nhập Google | Google email đã có với role=DRIVER | `200`, JWT |
| Email của STAFF dùng Google | Email `staff@BiteBolt.com` | `403 ERROR_SSO_REQUIRED` |
| Tài khoản bị LOCKED | status=LOCKED | `403 ERROR_ACCOUNT_LOCKED` |
| CSRF state mismatch | Callback với state sai | `400 ERROR_SSO_STATE_MISMATCH` |
| Mobile ID Token verify | `POST /social/google/verify-id-token` + valid idToken | `200`, token trong body |

---

## 15. 📋 Checklist Triển Khai (Implementation Checklist)

### Prerequisites (Phase 1 phải hoàn thành trước):
- [ ] `AuthMethod` enum đã tồn tại
- [ ] Flyway migration V2 đã chạy (auth_method, email columns)
- [ ] `Credential.entity` đã có `email` và `authMethod` fields

### Phase 2 Tasks:
- [ ] DevOps: Tạo Google OAuth App trên Google Cloud Console, lấy Client ID + Secret
- [ ] Thêm dependency `spring-boot-starter-oauth2-client` nếu chưa có từ Phase 1
- [ ] Tạo Flyway migration `V3__add_google_social_fields.sql`
- [ ] Cập nhật `Credential.java` — thêm `googleSub`
- [ ] Tạo `GoogleOAuthProperties.java`
- [ ] Cập nhật `auth-service.properties` với Google config
- [ ] Tạo `SocialAuthController.java`
- [ ] Tạo `GoogleSocialAuthServiceImpl.java` với full callback + guard logic
- [ ] Thêm endpoint `/social/google/verify-id-token` cho Mobile App SDK flow
- [ ] Cập nhật gRPC proto trong `common-grpc` — thêm `CreateUserFromSocialLogin` RPC
- [ ] Implement gRPC handler trong `user-service`
- [ ] Thêm message codes mới vào `AuthMessageConstant.java` + resource bundles
- [ ] Thêm `GOOGLE_STATE_PREFIX` vào `AuthRedisConstant.java`
- [ ] Update `CredentialRepository.java` — thêm `findByGoogleSub`
- [ ] Manual testing toàn bộ test cases
