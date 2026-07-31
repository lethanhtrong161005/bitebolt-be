# BiteBolt User Management Plan

## 1. Overview
The User Management feature is handled by `user-service`. It provides internal STAFF and ADMIN roles the ability to view, manage, and audit users (including SHIPPER and CUSTOMER profiles).

## 2. API Endpoints (Already Implemented)

### Get Users (Paginated)
- **Endpoint**: `GET /api/v1/users`
- **Controller**: `UserController.getAllUsers(Pageable pageable)`
- **Important**: Uses `@ParameterObject Pageable pageable` to render query parameters correctly in Swagger UI.
- **Service Layer**: Filters out deleted users (`isDeleted = false`).

### Get User by ID
- **Endpoint**: `GET /api/v1/users/{userId}`
- **Behavior**: Retrieves user details. Throws `ERROR_USER_NOT_FOUND` if the user does not exist or is soft-deleted.

### Update User
- **Endpoint**: `PUT /api/v1/users/{userId}`
- **Body**: `{ fullName, avatar, status }`
- **Behavior**: Updates user details. Cannot update role or phone number through this endpoint.

### Soft Delete User
- **Endpoint**: `DELETE /api/v1/users/{userId}`
- **Behavior**: Marks `isDeleted = true`. DOES NOT physically delete the row.

## 3. Data Access Rules (CRITICAL)

### The `isDeleted` Gotcha
- The `User` entity uses a `Boolean isDeleted` field for soft deletion.
- **DO NOT** use Spring Data method name derivation like `findByIsDeletedFalse()`. This causes parser errors in our current Spring Boot version because of how the boolean field is named and parsed.
- **ALWAYS** use `@Query` with explicit JPQL:
  ```java
  @Query("SELECT u FROM User u WHERE u.isDeleted = false")
  Page<User> findAllActiveUsers(Pageable pageable);
  ```
- Make sure to alias table names (`User u`) and refer to fields precisely (`u.isDeleted = false`).

## 4. Error Handling
- Use `UserMessageConstant.java` for domain-specific errors.
- Do NOT use cross-service message keys (e.g., from `auth-service`).
