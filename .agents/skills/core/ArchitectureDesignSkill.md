# 🏗️ Architecture Design Skill

**Category**: Core | **Use When**: Designing a new service, a new module inside an existing service, or a new API surface.

---

## 1. Design Workflow

1. **Clarify the business need** — read/request the relevant doc in `docs/business/`; do not design from assumptions.
2. **Define the API contract first**: request/response DTOs, HTTP verbs & paths (external) or `.proto` messages (internal). Get this reviewed before writing implementation code.
3. **Choose communication style** per `rules/ARCHITECTURE_RULES.md`:
   - External client ↔ backend: **REST** (JSON, versioned `/api/v1/...`).
   - Service ↔ service (internal): **gRPC**.
   - Fire-and-forget / async / broadcast: **Kafka event**.
4. **Define the data model**: entity, indexes, soft-delete flag, audit columns (`createdAt`, `updatedAt`, `createdBy`, `isDeleted`). Write the migration script under `templates/sql/`.
5. **Define layering**: Controller (HTTP/gRPC adapter, validation) → Service (business logic, transactions) → Repository (persistence). No layer skipping.
6. **Define error handling**: which exceptions can occur, map each to an HTTP status / gRPC status per the exception hierarchy in `rules/ARCHITECTURE_RULES.md`.
7. **Define security boundary**: what roles/scopes can call this API (RBAC), what data must be scoped to the caller's tenant/user.
8. **Define observability**: what gets logged (never sensitive fields), what metrics/traces are emitted (MDC correlation id).

## 2. Design Review Checklist
- [ ] API contract documented before implementation.
- [ ] Communication style matches `ARCHITECTURE_RULES.md` (gRPC internal / REST external).
- [ ] DTOs separate from entities.
- [ ] Transaction boundaries explicit.
- [ ] Soft delete applied where the entity represents business data.
- [ ] Failure modes enumerated with corresponding error codes.
- [ ] Security/RBAC requirements stated.

## 3. Related References
- `rules/ARCHITECTURE_RULES.md`
- `skills/microservices/gRPCImplementationSkill.md`
- `skills/microservices/KafkaIntegrationSkill.md`
- `templates/java/ServiceTemplate.java`, `templates/java/ControllerTemplate.java`
