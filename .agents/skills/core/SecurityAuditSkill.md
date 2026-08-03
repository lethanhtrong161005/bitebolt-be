# 🔐 Security Audit Skill

**Category**: Core | **Use When**: Reviewing code for vulnerabilities, before merging any change that touches auth, payment, or PII, or when asked for a "security review".

---

## 1. Audit Checklist

### Authentication & Authorization
- [ ] JWT validated at the API Gateway; internal services trust the propagated identity, not raw client input.
- [ ] Every endpoint declares required role(s)/scope(s) — no endpoint left with an implicit "any authenticated user" when it should be restricted.
- [ ] Object-level authorization checked (a user can only access **their own** resource, not just "any authenticated user").

### Input Validation
- [ ] All external input validated (`@Valid`, `@NotNull`, `@Size`, custom validators) at the controller boundary.
- [ ] No string concatenation used to build SQL/JPQL (use parameterized queries / JPA methods only).
- [ ] File uploads validated for type/size; never trust client-supplied `Content-Type`.

### Sensitive Data
- [ ] Passwords hashed (never stored or logged in plain text).
- [ ] No password, OTP, token, card number, or other secret appears in log statements — check every `log.info/debug/error` in the diff.
- [ ] PII fields masked in logs (e.g., email → `j***@example.com`).
- [ ] Secrets/config (DB creds, API keys) come from environment/secret manager, never hard-coded.

### Data Lifecycle
- [ ] Soft delete (`isDeleted`) used for business data instead of hard delete, unless there's a legal requirement to purge.
- [ ] Deleted/inactive records excluded from default queries.

### Transport & Dependencies
- [ ] Internal gRPC calls use TLS in production configs.
- [ ] No newly-added dependency with known critical CVEs (spot-check version against advisories when adding a new library).

## 2. Severity Classification
Use the same tags as `skills/core/CodeReviewSkill.md` (`[BLOCKER]`, `[MAJOR]`, `[MINOR]`). Any finding under **Authentication & Authorization** or **Sensitive Data** defaults to `[BLOCKER]` unless proven otherwise.

## 3. Related References
- `rules/ARCHITECTURE_RULES.md` (security patterns section)
- `docs/security/` (auth plans, gateway security, user context)
- `skills/core/CodeReviewSkill.md`
