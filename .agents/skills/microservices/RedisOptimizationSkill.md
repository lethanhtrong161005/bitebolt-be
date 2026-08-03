# ⚡ Redis Optimization Skill

**Category**: Microservices | **Use When**: Adding caching, session storage, or rate limiting.

## 1. Decide What To Cache
Cache read-heavy, rarely-changing data (reference data, computed aggregates, session state). Do **not** cache data that must always be strongly consistent (e.g., current account balance) without an explicit invalidation strategy.

## 2. Step-by-Step
1. Choose a namespaced key pattern: `<service>:<entity>:<id>` (e.g., `user-service:profile:123`).
2. Set an explicit TTL on every key — no cache entry should live forever by accident.
3. Invalidate the cache key on write (update/delete) in the same Service-layer transaction that mutates the source of truth, not as an afterthought.
4. For session data, store only the minimal claims needed (user id, roles) — never store full PII objects in session cache.
5. Guard against cache stampede on hot keys (e.g., short-lived lock or request coalescing) if the underlying query is expensive.

## 3. Checklist
- [ ] Key naming follows `<service>:<entity>:<id>` pattern.
- [ ] TTL set explicitly.
- [ ] Cache invalidated on write, same transaction as the source-of-truth update.
- [ ] No full PII objects cached in session data.
- [ ] Hot-key stampede considered for expensive lookups.

## 4. Related References
- `rules/ARCHITECTURE_RULES.md`
