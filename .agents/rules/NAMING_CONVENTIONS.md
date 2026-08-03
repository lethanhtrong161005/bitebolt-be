# 🏷️ Naming Conventions (MANDATORY)

| Element | Convention | Example |
|---------|-----------|---------|
| Class / Interface | `PascalCase` | `OrderService`, `PaymentClient` |
| Method / Variable | `camelCase` | `calculateTotal()`, `orderId` |
| Constant | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Package | lowercase, dot-separated | `com.bitebolt.order.service` |
| REST endpoint path | kebab-case, plural nouns | `/api/v1/order-items` |
| DB table | `snake_case`, plural | `order_items` |
| DB column | `snake_case` | `created_at`, `is_deleted` |
| DTO suffix | `Request` / `Response` / `Dto` | `CreateOrderRequest`, `OrderResponse` |
| Entity | singular noun, no suffix | `Order`, `OrderItem` |
| Exception | suffix `Exception` | `OrderNotFoundException` |
| Test class | suffix `Test` | `OrderServiceTest` |
| Boolean field/method | prefix `is`/`has`/`can` | `isDeleted`, `hasPermission()` |

## Rules
- No abbreviations unless industry-standard (`id`, `url`, `dto`).
- No Vietnamese, no diacritics, no non-ASCII characters in identifiers.
- Avoid generic names (`data`, `info`, `manager`, `helper`) without a qualifying prefix.
