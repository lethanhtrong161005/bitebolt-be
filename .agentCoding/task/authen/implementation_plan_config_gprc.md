# Fix Plan: Xử lý gRPC Error Mapping (NOT_FOUND vs UNAVAILABLE)

## Phân Tích Nguyên Nhân (Root Cause Analysis)

Khi user không tồn tại trong `user-service` (Not Found), hệ thống trả về lỗi:
```json
{
  "status": 503,
  "message": {
    "code": "ERROR_USER_SERVICE_UNAVAILABLE"
  }
}
```
Nhưng mong muốn thực tế là phải trả về:
```json
{
  "status": 401,
  "message": {
    "code": "ERROR_USER_NOT_FOUND"
  }
}
```

### Tại sao lại ra lỗi 503?
Có 3 khả năng:
1. **Lỗi gRPC Status Code không phải là `NOT_FOUND`**:
   - Nếu `user-service` bị lỗi kết nối cơ sở dữ liệu khi truy vấn (chưa khởi tạo bảng `users`, sai mật khẩu db...), nó sẽ quăng exception và nhảy vào block `catch (Exception e)` trên server gRPC, trả về `Status.INTERNAL` (lỗi 500) hoặc `Status.UNKNOWN`.
   - Client nhận được `INTERNAL`/`UNKNOWN` khác với `NOT_FOUND` nên tự động ném ra `503 ERROR_USER_SERVICE_UNAVAILABLE`.
2. **Sai lệch thư viện `io.grpc.Status`**:
   - Trong một số phiên bản, so sánh `e.getStatus().getCode() == Status.Code.NOT_FOUND` cần so sánh chính xác qua `.equals()` hoặc kiểm tra qua `instanceof`.
3. **Lỗi parse UUID**:
   - Nếu định dạng UUID của `userId` từ `credentials` không hợp lệ, server quăng `Status.INVALID_ARGUMENT`, client nhận được và chuyển thành 503.

### Giải pháp
1. **Cải tiến Client Log & Mapping**:
   - Thêm log in ra chi tiết `StatusRuntimeException` (gồm Code và Description) tại [UserGrpcClient.java](file:///d:/workspace/EcoMove/eco-move-backend/core-services/auth-service/src/main/java/com/ecomove/auth/client/UserGrpcClient.java).
   - Xử lý mapping linh hoạt hơn: Nếu code là `NOT_FOUND` hoặc tin nhắn chứa cụm từ `not found`, trả về `401 ERROR_USER_NOT_FOUND`.
2. **Khởi tạo dữ liệu mẫu cho `user-service`**:
   - Viết migration SQL khởi tạo bảng `users` và seed dữ liệu tương ứng với `userId` của `credentials` để loại bỏ khả năng lỗi database.

---

## Proposed Changes

### Component 1: auth-service – gRPC Client Error Handling

---

#### [MODIFY] [UserGrpcClient.java](file:///d:/workspace/EcoMove/eco-move-backend/core-services/auth-service/src/main/java/com/ecomove/auth/client/UserGrpcClient.java)
Cải tiến bắt lỗi gRPC, in chi tiết trạng thái lỗi lỗi để debug và map chuẩn mã lỗi:

```java
package com.ecomove.auth.client;

import com.ecomove.common.exception.HttpException;
import com.ecomove.grpc.user.GetUserProfileRequest;
import com.ecomove.grpc.user.UserProfileResponse;
import com.ecomove.grpc.user.UserGrpcServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UserGrpcClient {

    @GrpcClient("user-service")
    private UserGrpcServiceGrpc.UserGrpcServiceBlockingStub userGrpcStub;

    public UserProfileResponse getUserProfile(UUID userId) {
        log.info("Calling user-service via gRPC for user profile ID: {}", userId);
        try {
            GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                    .setUserId(userId.toString())
                    .build();

            return userGrpcStub
                    .withDeadlineAfter(3, TimeUnit.SECONDS)
                    .getUserProfile(request);

        } catch (StatusRuntimeException e) {
            Status.Code code = e.getStatus().getCode();
            String description = e.getStatus().getDescription();
            log.error("gRPC error occurred. Code: {}, Description: {}", code, description, e);

            // Map gRPC NOT_FOUND thành ERROR_USER_NOT_FOUND (HTTP 401)
            if (code == Status.Code.NOT_FOUND || 
                (description != null && description.toLowerCase().contains("not found"))) {
                log.warn("User profile not found for ID: {}", userId);
                throw new HttpException(401, "ERROR_USER_NOT_FOUND");
            }
            
            // Map gRPC INVALID_ARGUMENT thành HTTP 400
            if (code == Status.Code.INVALID_ARGUMENT) {
                throw new HttpException(400, "ERROR_INVALID_USER_ID");
            }

            throw new HttpException(503, "ERROR_USER_SERVICE_UNAVAILABLE");
        }
    }
}
```

---

### Component 2: user-service – DB Migration / Init SQL

---

#### [NEW] `V2__seed_test_user.sql` trong `user-service`
Tạo dữ liệu người dùng đồng bộ với credential ID `e81bb380-4966-4194-a15d-4f1073860bb4` trong `user_db`:

```sql
INSERT INTO users (
  user_id, 
  full_name, 
  email, 
  avatar, 
  kyc_status, 
  rating,
  created_at, 
  updated_at, 
  created_by, 
  updated_by, 
  is_deleted
) VALUES (
  'e81bb380-4966-4194-a15d-4f1073860bb4', 
  'Nguyễn Văn A', 
  'testuser@ecomove.com', 
  'https://cdn.ecomove.vn/avatar.png', 
  'VERIFIED',
  5.0,
  NOW(), 
  NOW(), 
  'system', 
  'system', 
  FALSE
) ON CONFLICT (user_id) DO NOTHING;
```

---

## Verification Plan

### Manual Verification
1. Restart **user-service** và **auth-service**.
2. Gọi API `POST /api/v1/auth/verify-otp`:
   - Nếu user hợp lệ → trả về thành công 200 kèm access token enriched.
   - Nếu truyền vào user đã bị xóa → Kiểm tra log của `auth-service` để biết gRPC code trả về và xác nhận client ném lỗi `401 ERROR_USER_NOT_FOUND`.
