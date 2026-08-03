# BiteBolt Authentication Business Design

> **Phiên bản**: 2.0 | **Cập nhật lần cuối**: 2026-07-31  
> **Tác giả**: BiteBolt Backend Team  
> **Phạm vi**: Authentication Service (`auth-service`) — Chiến lược xác thực theo vai trò

---

## 1. 🏢 Tổng Quan Hệ Thống Vai Trò

BiteBolt hoạt động với **4 vai trò người dùng** được phân chia thành **2 nhóm** với chiến lược xác thực hoàn toàn khác nhau:

### Bảng Phân Loại Vai Trò

| Vai trò | Nhóm | Mô tả |
|---|---|---|
| **`ADMIN`** | 🏢 Internal (Nội bộ) | Quản trị viên hệ thống, cấu hình, phân quyền, quản lý thương hiệu |
| **`STAFF`** | 🏢 Internal (Nội bộ) | Nhân viên quản lý đơn hàng, duyệt hồ sơ SHIPPER, quản lý menu |
| **`SHIPPER`** | 📱 External (Bên ngoài) | Nhân viên giao hàng (nhận đơn, giao đồ ăn) |
| **`CUSTOMER`** | 📱 External (Bên ngoài) | Khách hàng đặt đồ ăn |

---

## 2. 🔐 Chiến Lược Xác Thực Theo Vai Trò (Authentication Strategy)

### 🏢 Nhóm Nội Bộ — `ADMIN` & `STAFF`

#### Phương thức xác thực: **Microsoft Entra ID SSO (OAuth 2.0 / OIDC)**

Toàn bộ nhân viên nội bộ **BẮT BUỘC** phải đăng nhập thông qua **Microsoft Entra ID**.
- Tích hợp với hệ thống quản lý danh tính nội bộ.
- Hỗ trợ **Multi-Factor Authentication (MFA)** bắt buộc.
- **Single Sign-On (SSO)**.

#### ❌ Quy tắc cứng — NGHIÊM CẤM:
- `ADMIN` và `STAFF` **KHÔNG ĐƯỢC PHÉP** đăng nhập bằng số điện thoại + mật khẩu hoặc Google.
- Nếu phát hiện tài khoản nội bộ cố đăng nhập qua endpoint public (`/api/v1/auth/login`), hệ thống phải từ chối và trả về lỗi `403 FORBIDDEN`.

#### Email Domain Validation:
- Chỉ chấp nhận email có domain **`@bitebolt.com`** (hoặc tên miền tổ chức được cấu hình).

---

### 📱 Nhóm Bên Ngoài — `SHIPPER` & `CUSTOMER`

#### Phương thức xác thực: **Đa phương thức (Multi-Auth)**

Người dùng bên ngoài có thể đăng nhập bằng nhiều phương thức:

#### Phương thức 1: Số điện thoại + Mật khẩu + 2FA OTP
- Nhập số điện thoại + mật khẩu -> Gửi OTP -> Xác thực OTP -> Trả JWT.

#### Phương thức 2: Social Login — Google (OAuth 2.0)
- Đăng nhập Google OAuth -> Auto register (nếu chưa có) -> Trả JWT.

> **Lưu ý quan trọng**: Nếu một email Google trùng với email của tài khoản `ADMIN`/`STAFF`, hệ thống phải **từ chối** đăng nhập.

---

## 3. 🔒 Ma Trận Phân Quyền Xác Thực

| Phương thức | ADMIN | STAFF | SHIPPER | CUSTOMER |
|---|:---:|:---:|:---:|:---:|
| Microsoft Entra ID SSO | ✅ | ✅ | ❌ | ❌ |
| Số điện thoại + Mật khẩu + OTP | ❌ | ❌ | ✅ | ✅ |
| Google Social Login | ❌ | ❌ | ✅ | ✅ |

---

## 4. 🛡️ Quy Tắc Bảo Mật Bắt Buộc (Security Rules)

1. **Role separation**: ADMIN/STAFF không bao giờ được dùng endpoint `/api/v1/auth/login` (phone+pass).
2. **Domain enforcement**: SSO callback phải validate email domain của Entra ID token.
3. **Role assignment**: Role `ADMIN` chỉ được gán thủ công.
4. **Token scope**: JWT payload phải chứa `role` claim (`ADMIN`, `STAFF`, `SHIPPER`, `CUSTOMER`).
5. **SHIPPER verification**: `SHIPPER` cần hoàn tất xác minh hồ sơ (CCCD, bằng lái) do `STAFF` duyệt trước khi được nhận đơn hàng.

---

## 5. 📋 Trách Nhiệm Của Từng Vai Trò

### `ADMIN`
- Quản lý cấu hình hệ thống toàn cục.
- Phân quyền và tạo tài khoản STAFF.
- Xem báo cáo doanh thu, thống kê kinh doanh.
- Quản lý chiến dịch khuyến mãi toàn hệ thống.

### `STAFF`
- Quản lý menu (món ăn, combo).
- Xử lý đơn hàng, điều phối giao hàng nếu cần.
- Duyệt hồ sơ đăng ký của SHIPPER.
- Xử lý khiếu nại của CUSTOMER và SHIPPER.

### `SHIPPER`
- Nhận và thực hiện đơn giao đồ ăn.
- Cập nhật trạng thái (đang lấy món, đang giao, đã giao).
- Xem lịch sử thu nhập.

### `CUSTOMER`
- Xem menu, chọn món, đặt đồ ăn.
- Thanh toán và đánh giá dịch vụ/món ăn.
- Theo dõi đơn hàng real-time.

---

## 6. 📌 Hướng Dẫn Cho Agents — Coding Standards

### 6.1. Endpoint Segregation
```
# External Users (SHIPPER, CUSTOMER)
POST   /api/v1/auth/login
POST   /api/v1/auth/verify-otp
GET    /api/v1/auth/social/google
GET    /api/v1/auth/social/google/callback

# Internal Users (ADMIN, STAFF)
GET    /api/v1/auth/sso/entra
GET    /api/v1/auth/sso/entra/callback
```

### 6.2. Role Validation Constant
Trong `Role.java`:
```java
Set<Role> INTERNAL_ROLES = Set.of(Role.ADMIN, Role.STAFF);
Set<Role> EXTERNAL_ROLES = Set.of(Role.SHIPPER, Role.CUSTOMER);
```

### 6.3. JWT Claims Convention
```json
{
  "sub": "userId (UUID)",
  "role": "ADMIN | STAFF | SHIPPER | CUSTOMER",
  "authMethod": "SSO_ENTRA | PHONE_OTP | GOOGLE"
}
```
