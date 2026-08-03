# Phase 1 — Internal SSO Authentication Plan
## ADMIN & STAFF via Microsoft Entra ID

> **Phase**: 1 (Ưu tiên cao nhất)  
> **Phạm vi**: Xác thực nội bộ cho `ADMIN` và `STAFF`  
> **Phương thức**: Microsoft Entra ID (Azure AD) — OAuth 2.0 Authorization Code Flow + OIDC  
> **Tài liệu tham chiếu**: [AUTH_BUSINESS_DESIGN.md](file:///d:/workspace/BiteBolt/bitebolt-backend/.agents/AUTH_BUSINESS_DESIGN.md)

---

## 1. 🎯 Mục Tiêu

Implement luồng đăng nhập SSO dành cho nhân viên nội bộ (`ADMIN`, `STAFF`) thông qua **Microsoft Entra ID**, đảm bảo:
- Nhân viên chỉ đăng nhập bằng tài khoản Microsoft công ty (`@BiteBolt.com`).
- Ngăn chặn hoàn toàn việc ADMIN/STAFF đăng nhập bằng phone+password.
- Auto-provision tài khoản STAFF khi đăng nhập lần đầu.
- Phát hành BiteBolt JWT sau khi xác thực Entra ID thành công.

---

## 2. 📦 Dependencies Cần Thêm vào `auth-service/pom.xml`

```xml
<!-- Spring Security OAuth2 Client (OIDC) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Microsoft MSAL4J — Thư viện xác thực Microsoft -->
<dependency>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>msal4j</artifactId>
    <version>1.17.2</version>
</dependency>
```

> **Lý do chọn MSAL4J**: Spring Security OAuth2 client xử lý redirect và code exchange, nhưng MSAL4J cung cấp hỗ trợ đặc thù cho Entra ID (token caching, tenant-aware, refresh token management).

---

## 3. 🗄️ Database Schema Changes (Flyway Migration)

### 3.1. Mở rộng bảng `credentials` hiện tại

File migration: `V2__add_auth_method_and_sso_fields.sql`

```sql
-- Thêm enum type cho auth method
ALTER TABLE credentials 
    ADD COLUMN auth_method VARCHAR(20) NOT NULL DEFAULT 'PHONE_OTP',
    ADD COLUMN email VARCHAR(255) UNIQUE,
    ADD COLUMN entra_object_id VARCHAR(255) UNIQUE;

-- Cập nhật constraint: phone có thể nullable (SSO users không có phone)
ALTER TABLE credentials ALTER COLUMN phone DROP NOT NULL;
ALTER TABLE credentials ALTER COLUMN password_hash DROP NOT NULL;

-- Index tăng tốc SSO lookup
CREATE UNIQUE INDEX idx_credentials_entra_oid ON credentials(entra_object_id) WHERE entra_object_id IS NOT NULL;
CREATE UNIQUE INDEX idx_credentials_email ON credentials(email) WHERE email IS NOT NULL;
```

### 3.2. Thêm enum `AuthMethod` (Java)

**File**: `com.bitebolt.auth.enums.AuthMethod`

```java
public enum AuthMethod {
    PHONE_OTP,   // Phone + Password + OTP (DRIVER, RIDER)
    SSO_ENTRA,   // Microsoft Entra ID SSO (ADMIN, STAFF)
    GOOGLE       // Google Social Login (DRIVER, RIDER)
}
```

### 3.3. Cập nhật `Credential.java` entity

```java
@Enumerated(EnumType.STRING)
@Column(name = "auth_method", nullable = false)
private AuthMethod authMethod;

@Column(name = "email", unique = true)
private String email;

@Column(name = "entra_object_id", unique = true)
private String entraObjectId;
```

---

## 4. ⚙️ Configuration Changes

### 4.1. Thêm `EntraProperties.java` — `com.bitebolt.auth.config`

```java
@Data
@Component
@ConfigurationProperties(prefix = "entra")
public class EntraProperties {
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String allowedEmailDomain;  // e.g. "BiteBolt.com"
}
```

### 4.2. Thêm vào `auth-service.properties`

```properties
# Microsoft Entra ID SSO Config
entra.tenant-id=${ENTRA_TENANT_ID}
entra.client-id=${ENTRA_CLIENT_ID}
entra.client-secret=${ENTRA_CLIENT_SECRET}
# CHÚ Ý: Redirect URI PHẢI trỏ về API Gateway (port 8080 ở dev hoặc domain chung ở prod)
# chứ không được trỏ trực tiếp về port 8082 của auth-service.
entra.redirect-uri=http://localhost:8080/api/v1/auth/sso/entra/callback
entra.allowed-email-domain=BiteBolt.com
```

> **Lưu ý bảo mật**: `client-secret` phải được inject qua environment variable, không được commit vào properties file.

### 4.3. Đăng ký Application trên Azure Portal

Các bước thực hiện ngoài code (yêu cầu IT/DevOps):
1. Vào **Azure Portal** → **Microsoft Entra ID** → **App Registrations** → **New Registration**.
2. Đặt tên: `BiteBolt-auth-service`.
3. Redirect URI: Cấu hình trỏ về **API Gateway** của hệ thống:
   - Development (Local): `http://localhost:8080/api/v1/auth/sso/entra/callback`
   - Production: `https://api.BiteBolt.com/api/v1/auth/sso/entra/callback`
4. Tab **Certificates & secrets** → tạo **Client Secret**.
5. Tab **API permissions** → thêm `openid`, `profile`, `email`, `User.Read`.
6. Ghi lại: `Tenant ID`, `Client ID`, `Client Secret`.

---

## 5. 📁 Cấu Trúc File Mới Cần Tạo

```
com.bitebolt.auth
├── config/
│   └── EntraProperties.java          [NEW]
├── constant/
│   └── AuthMessageConstant.java       [MODIFY] — thêm SSO error codes
├── controller/
│   └── SsoController.java             [NEW]
├── dto/
│   ├── request/
│   │   └── (không cần request body — redirect flow)
│   └── response/
│       └── SsoLoginResponse.java      [NEW]
├── enums/
│   ├── AuthMethod.java                [NEW]
│   └── Role.java                      [MODIFY] — đã có STAFF
├── entity/
│   └── Credential.java                [MODIFY] — thêm authMethod, email, entraObjectId
├── service/
│   ├── AuthService.java               [MODIFY] — thêm method SSO
│   └── impl/
│       ├── AuthServiceImpl.java        [MODIFY] — guard phone login cho ADMIN/STAFF
│       └── EntraSsoServiceImpl.java    [NEW]
└── repository/
    └── CredentialRepository.java      [MODIFY] — thêm findByEmail, findByEntraObjectId
```

---

## 6. 🔌 API Endpoints Cần Implement

### `SsoController.java`

```
GET  /api/v1/auth/sso/entra
     → Tạo Authorization URL và redirect người dùng đến Microsoft login page.

GET  /api/v1/auth/sso/entra/callback?code=xxx&state=xxx
     → Nhận Authorization Code, exchange lấy token, validate, issue BiteBolt JWT.
```

**Luồng chi tiết:**

```
[Admin Panel]
    │ GET /api/v1/auth/sso/entra
    ▼
[SsoController.initiateEntraLogin()]
    │ Build Microsoft Authorization URL:
    │   https://login.microsoftonline.com/{tenantId}/oauth2/v2.0/authorize
    │   ?client_id={clientId}
    │   &response_type=code
    │   &redirect_uri={redirectUri}
    │   &scope=openid profile email
    │   &state={randomState}  ← lưu state vào Redis (CSRF protection)
    │ Redirect 302 → Microsoft Login Page
    ▼
[Microsoft Entra ID — User nhập work account + MFA]
    │ Thành công → Redirect đến /api/v1/auth/sso/entra/callback?code=xxx&state=xxx
    ▼
[SsoController.handleEntraCallback()]
    │ 1. Validate state param với Redis (chống CSRF)
    │ 2. Exchange code → Microsoft Access Token + ID Token
    │ 3. Decode ID Token → extract: email, name, oid (object ID)
    │ 4. Validate email domain (phải là @BiteBolt.com)
    │ 5. Lookup Credential bằng entraObjectId hoặc email
    │    - Nếu chưa có & email @BiteBolt.com → auto-provision STAFF
    │    - ADMIN không bao giờ được auto-provision (chỉ có qua Admin Panel)
    │    - Nếu tài khoản bị LOCKED → 403
    │ 6. Generate BiteBolt JWT (accessToken + refreshToken)
    │    - claim: authMethod = "SSO_ENTRA"
    │ 7. Set token vào HttpOnly Cookie (Web mode)
    │ 8. Redirect về Admin Panel dashboard URL
    ▼
[Admin Panel — đã đăng nhập thành công]
```

---

## 7. 🛡️ Guard: Chặn ADMIN/STAFF Dùng Phone Login

Trong `AuthServiceImpl.login()`, **thêm validation** sau khi tìm thấy Credential:

```java
// Guard: ADMIN và STAFF không được phép dùng phone+password
if (INTERNAL_ROLES.contains(credential.getRole())) {
    throw new HttpException(403, AuthMessageConstant.ERROR_SSO_REQUIRED);
}
```

`INTERNAL_ROLES` định nghĩa trong `AuthServiceImpl` (hoặc constant riêng):
```java
private static final Set<Role> INTERNAL_ROLES = Set.of(Role.ADMIN, Role.STAFF);
```

---

## 8. 📨 Message Codes Cần Thêm vào `AuthMessageConstant.java`

```java
/**
 * VI: Vai trò này bắt buộc phải đăng nhập qua Microsoft SSO.
 * EN: This role must authenticate via Microsoft SSO.
 */
public static final String ERROR_SSO_REQUIRED = "ERROR_SSO_REQUIRED";

/**
 * VI: Email không thuộc tên miền nội bộ được chấp nhận.
 * EN: Email domain is not authorized for internal access.
 */
public static final String ERROR_INVALID_EMAIL_DOMAIN = "ERROR_INVALID_EMAIL_DOMAIN";

/**
 * VI: Phiên SSO không hợp lệ hoặc đã hết hạn.
 * EN: SSO session is invalid or has expired.
 */
public static final String ERROR_SSO_STATE_MISMATCH = "ERROR_SSO_STATE_MISMATCH";

/**
 * VI: Đăng nhập SSO thành công.
 * EN: SSO login successful.
 */
public static final String SUCCESS_SSO_LOGIN = "SUCCESS_SSO_LOGIN";
```

---

## 9. 🔑 Redis Keys Cần Thêm vào `AuthRedisConstant.java`

```java
/** Prefix lưu state ngẫu nhiên để chống CSRF trong OAuth flow */
public static final String SSO_STATE_PREFIX = "SSO_STATE:";
// TTL: 10 phút — đủ thời gian user hoàn tất login Microsoft
```

---

## 10. 🧪 Verification Plan

### Manual Testing (Postman / Browser):

| Test Case | Input | Expected Output |
|---|---|---|
| Redirect to Entra | `GET /api/v1/auth/sso/entra` | `302` redirect đến Microsoft login |
| STAFF tự tạo qua SSO | Email `staff1@BiteBolt.com` đăng nhập lần đầu | Auto-provision, `201` role=STAFF, JWT cookie |
| ADMIN SSO | Email `admin@BiteBolt.com` (đã có trong DB) | `200`, JWT cookie với role=ADMIN |
| Email cá nhân | Email `hacker@gmail.com` | `403 ERROR_INVALID_EMAIL_DOMAIN` |
| ADMIN dùng phone login | `POST /api/v1/auth/login` với phone của ADMIN | `403 ERROR_SSO_REQUIRED` |
| STAFF dùng phone login | `POST /api/v1/auth/login` với phone của STAFF | `403 ERROR_SSO_REQUIRED` |
| CSRF state mismatch | Callback với state sai | `400 ERROR_SSO_STATE_MISMATCH` |

---

## 11. 📋 Checklist Triển Khai (Implementation Checklist)

- [ ] IT/DevOps: Đăng ký App trên Azure Portal, lấy Tenant ID, Client ID, Client Secret
- [ ] Thêm dependency `spring-boot-starter-oauth2-client` và `msal4j` vào `pom.xml`
- [ ] Tạo Flyway migration `V2__add_auth_method_and_sso_fields.sql`
- [ ] Tạo enum `AuthMethod.java`
- [ ] Cập nhật `Credential.java` entity với các field mới
- [ ] Tạo `EntraProperties.java` config bean
- [ ] Cập nhật `auth-service.properties` với Entra config
- [ ] Cập nhật `CredentialRepository.java` thêm `findByEmail`, `findByEntraObjectId`
- [ ] Tạo `SsoController.java` với 2 endpoints
- [ ] Implement `EntraSsoServiceImpl.java` với full callback logic
- [ ] Update `AuthServiceImpl.login()` — thêm guard cho INTERNAL_ROLES
- [ ] Thêm message codes vào `AuthMessageConstant.java` + `messages_vi.properties` + `messages_en.properties`
- [ ] Thêm Redis key vào `AuthRedisConstant.java`
- [ ] Update `SecurityConfig.java` — thêm callback endpoint vào PUBLIC_PATHS
- [ ] Manual testing toàn bộ test cases
