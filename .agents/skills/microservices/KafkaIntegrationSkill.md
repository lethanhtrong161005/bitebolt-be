# 📨 Kafka Integration Skill

**Category**: Microservices | **Use When**: Publishing or consuming async/event-driven messages.

## 1. When To Use Kafka
Use for fire-and-forget domain events (e.g., `order.created`, `user.deleted`) that multiple downstream consumers may react to, or where the producer should not block on the consumer's processing.

## 2. Producing Events — Step-by-Step
1. Define the event schema (DTO) as an explicit, versioned class — never publish a raw JPA entity.
2. Choose a partition key that groups related events for the same aggregate (e.g., `userId`) to preserve ordering per entity.
3. Publish **after** the local transaction commits (use transactional outbox or `@TransactionalEventListener(phase = AFTER_COMMIT)`) to avoid publishing events for rolled-back transactions.
4. Never log the full event payload if it contains sensitive fields — log the event type and id only.

## 3. Consuming Events — Step-by-Step
1. Make the consumer idempotent (dedupe by event id) — Kafka delivers at-least-once, duplicates will happen.
2. Wrap processing in its own try/catch; route unprocessable messages to a dead-letter topic instead of blocking the partition.
3. Keep consumer logic thin — delegate to the existing Service layer, don't reimplement business rules in the listener.
4. Log consumer errors with enough context (event id, topic, offset) to replay manually if needed.

## 4. Checklist
- [ ] Event schema is a versioned DTO, not a raw entity.
- [ ] Publish happens after commit (outbox or `AFTER_COMMIT`).
- [ ] Partition key chosen deliberately.
- [ ] Consumer is idempotent.
- [ ] Dead-letter topic configured for poison messages.
- [ ] No sensitive data in event payload logs.

## 5. Related References
- `rules/ARCHITECTURE_RULES.md`
- `docs/observability/audit-logging/`
