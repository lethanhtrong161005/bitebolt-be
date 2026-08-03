# 🔐 Security Rules (MANDATORY)

These rules complement `skills/core/SecurityAuditSkill.md` and are enforced in every code review.

1. **Never log sensitive data**: password, OTP, access/refresh token, credit card number, national ID. Mask PII (email, phone) before logging.
2. **Never hard-code secrets**: DB credentials, API keys, signing keys must come from environment variables or a secret manager — never committed to source control.
3. **Validate all external input** at the controller boundary before it reaches the service layer.
4. **Authorize, not just authenticate**: every endpoint must check the caller is allowed to access *that specific resource*, not merely that they are logged in.
5. **Use soft delete** (`isDeleted`) for business/user data; hard delete only where legally required and explicitly approved.
6. **Parameterized queries only** — no string-concatenated SQL/JPQL.
7. **TLS required** for gRPC and REST calls outside local development.
8. **Dependency hygiene**: check new dependencies for known critical CVEs before adding them.

Any violation of rules 1–4 is treated as a `[BLOCKER]` in code review (see `skills/core/CodeReviewSkill.md`).
