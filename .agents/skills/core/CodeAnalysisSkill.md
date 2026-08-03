# 🧩 Code Analysis Skill

**Category**: Core | **Use When**: Exploring an unfamiliar part of the codebase, before modifying existing code, or when asked to "explain this module".

---

## 1. Goal
Understand existing code correctly **before** changing it, so generated changes are consistent with current patterns instead of introducing a new, conflicting style.

## 2. Step-by-Step Workflow

1. **Locate the layer**: identify whether the file is a Controller, Service, Repository, DTO, Entity, or Config — behavior expectations differ per layer (`rules/ARCHITECTURE_RULES.md`).
2. **Read top-down**: package → imports → class JavaDoc → fields → constructor → public methods → private methods.
3. **Trace the call chain**: for a given endpoint, follow Controller → Service → Repository/gRPC client → external system. Note every side effect (DB write, Kafka publish, cache write, external HTTP call).
4. **Identify existing conventions** in the file/module: naming style, exception types used, logging pattern, DTO mapping approach (MapStruct vs manual). Reuse them — do not introduce a second convention in the same module.
5. **Check for related business docs** in `docs/business/` or `docs/security/` that explain *why* the code behaves this way before assuming it's a bug.
6. **List dependencies**: which other services/classes call this class, and which classes it depends on (helps assess blast radius of a change).

## 3. Output Format When Reporting Analysis

```
## Analysis: OrderService

Layer: Service
Responsibilities: order creation, status transition, refund orchestration
Called by: OrderController, RefundKafkaListener
Depends on: OrderRepository, PaymentGrpcClient, InventoryGrpcClient, KafkaTemplate
Side effects: DB write (orders table), Kafka publish (order.created), gRPC call (PaymentService)
Notable patterns: soft delete via isDeleted, all mutations wrapped in @Transactional
Risks if modified: RefundKafkaListener assumes status transitions are synchronous — async change would break it
```

## 4. Anti-Patterns To Flag During Analysis
- Business logic living in the Controller layer.
- Repository/Entity leaking directly into API responses.
- Silent `catch (Exception e) {}` blocks.
- Hard-coded configuration values that should be in `application.yml`.

## 5. Related References
- `rules/ARCHITECTURE_RULES.md`
- `skills/core/ArchitectureDesignSkill.md`
- `docs/business/`, `docs/security/`
