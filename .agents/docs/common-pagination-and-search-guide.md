# 🔍 Enterprise Common Pagination & Generic Search Standard (`common-dto`)

**Status**: MANDATORY | **Module**: `common-libraries/common-dto` | **Scope**: All Microservices (Auth, User, Audit, etc.)

This document defines the architecture, standard class structures, and usage guidelines for implementing **Generic Search, Filtering, and Pagination** across all BiteBolt backend microservices.

---

## 📌 1. Architecture Overview

To ensure consistent API contracts, standard JSON envelopes, and clean separation of concerns, all search and pagination endpoints MUST utilize the common DTO classes provided in `common-libraries/common-dto`.

```
common-libraries/common-dto/
├── src/main/java/com/bitebolt/common/
│   ├── dto/
│   │   ├── request/
│   │   │   ├── BaseRequestParam.java        # Base class for GET URL Query Parameters
│   │   │   ├── PageRequest.java             # 1-based Page & Size container DTO
│   │   │   ├── SortOrder.java               # Enum (ASC, DESC)
│   │   │   ├── SortBaseCondition.java       # Abstract base for module-specific sorting
│   │   │   └── PayloadSearchRequest.java    # Generic POST JSON Search Payload wrapper <S, T>
│   │   ├── response/
│   │   │   └── PageResponse.java            # Enterprise standardized JSON Page envelope
│   │   └── specification/
│   │       └── GenericSpecification.java    # Dynamic JPA Criteria Predicate builder <E, T>
```

---

## 🛠️ 2. Core DTO Specifications

### 2.1 `BaseRequestParam` (For GET HTTP Query Requests)
Every request DTO that captures query string parameters from HTTP `GET` requests (such as `page`, `size`, `sortField`, `sortOrder`, `keyword`) MUST extend `BaseRequestParam`:

```java
package com.bitebolt.common.dto.request;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseRequestParam implements Serializable {
  @Builder.Default
  private Integer page = 1;

  @Builder.Default
  private Integer size = 10;

  private String sortField;

  @Builder.Default
  private SortOrder sortOrder = SortOrder.DESC;

  private String keyword;
}
```

---

### 2.2 `PageRequest` & `PageResponse<T>` (For API Responses)
All pagination REST endpoints MUST wrap their results in `PageResponse<DTO>` rather than returning raw Spring Data `Page<Entity>`:

```java
// Service Implementation Example
public PageResponse<UserResponseDto> searchUsers(UserSearchRequest request) {
  Pageable pageable = PageRequest.of(
      request.getPage() - 1, // Convert 1-based page to 0-based Spring Data Pageable
      request.getSize(),
      Sort.by(request.getSortOrder() == SortOrder.ASC ? Sort.Direction.ASC : Sort.Direction.DESC, 
              request.getSortField() != null ? request.getSortField() : "createdAt")
  );

  Page<UserEntity> pageResult = userRepository.findAll(
      new GenericSpecification<>(request.getKeyword(), "fullName"), 
      pageable
  );

  // Utility factory method of PageResponse
  return PageResponse.of(pageResult, userMapper::toDto);
}
```

---

### 2.3 `PayloadSearchRequest<S, T>` (For POST JSON Search Queries)
For complex multi-criteria filters sent via `POST /api/v1/{resource}/search`:

```java
// Request DTO definition
public class AuditLogSearchPayload {
  private String traceId;
  private String actorEmail;
  private String action;
  private String status;
}

// Controller Handler Example
@PostMapping("/search")
public ResponseEntity<ApiResponse<PageResponse<AuditLogResponseDto>>> searchAuditLogs(
    @Valid @RequestBody PayloadSearchRequest<AuditSortCondition, AuditLogSearchPayload> request
) {
  PageRequest pageReq = request.safePage();
  AuditLogSearchPayload searchCriteria = request.safeSearch();
  
  PageResponse<AuditLogResponseDto> response = auditLogService.search(searchCriteria, pageReq, request.safeSort());
  return ResponseEntity.ok(ApiResponse.success(response));
}
```

---

### 2.4 `GenericSpecification<E, T>` (For Dynamic Criteria Filtering)
`GenericSpecification` constructs non-blocking, type-safe JPA Criteria predicates dynamically without requiring custom JPQL string concatenations:

```java
// Constructs dynamic case-insensitive LIKE predicate for target field
Specification<UserEntity> spec = new GenericSpecification<>(request.getKeyword(), "email");

Page<UserEntity> users = userRepository.findAll(spec, pageable);
```

---

## 📋 3. Mandatory Development Rules

1. **1-Based Page Indexing**: All client API contracts accept `page` starting from `1`. Convert to zero-indexed `page - 1` when creating Spring Data `Pageable` instances.
2. **DTO Mapper Isolation**: Use Spring `@Component` helpers or Mappers (`UserMapper.java`, `AuditLogHelper.java`) inside `PageResponse.of(page, mapper)` instead of placing mapping logic inside entity classes or controllers.
3. **No Direct `Page<T>` Leakage**: Controllers MUST NEVER return `org.springframework.data.domain.Page<T>`. Always return `PageResponse<T>`.
4. **Standard Envelopes**: Controller responses MUST be wrapped in `ApiResponse<PageResponse<T>>`.

---
*Maintained by BiteBolt Architecture Team.*
