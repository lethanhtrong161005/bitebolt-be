# 🔍 Code Review Skill

**Category**: Core | **Use When**: Reviewing a pull request, reviewing another agent's output, or self-checking generated code before submission.

---

## 1. When To Use This Skill

- Before opening/approving a pull request.
- After generating code, as a **mandatory self-review pass** (agents must review their own diff before declaring a task done).
- When asked to "review this code", "check this PR", "code review", "audit this class".

---

## 2. Review Checklist (in order)

### Step 1 — Correctness
- [ ] Code compiles / no obvious syntax errors.
- [ ] Business logic matches the requirement in `docs/business/` (read the relevant doc first).
- [ ] Edge cases handled (null, empty list, 0/negative numbers, duplicate calls).
- [ ] No hidden side effects (e.g., method named `getX()` that also writes to DB).

### Step 2 — Standards Compliance (`rules/CODING_STANDARDS.md`)
- [ ] 2-space indentation, no tabs, max 100 chars/line.
- [ ] JavaDoc present on every public class and public method (`@param`, `@return`, `@throws`).
- [ ] Step-by-step inline comments inside method bodies for any method with >1 logical step.
- [ ] All comments and identifiers are in **English only**.
- [ ] Naming conventions followed: `camelCase` methods/fields, `PascalCase` classes, `UPPER_SNAKE_CASE` constants.

### Step 3 — Architecture Compliance (`rules/ARCHITECTURE_RULES.md`)
- [ ] Controller → Service → Repository layering respected (no repository calls from controller).
- [ ] DTOs used at API boundary; entities never returned directly from controllers.
- [ ] Internal service-to-service calls use gRPC; external client-facing calls use REST.
- [ ] Transactions (`@Transactional`) applied at the service layer, not controller/repository.
- [ ] Exceptions follow the project exception hierarchy (`rules/ARCHITECTURE_RULES.md#error-handling`).

### Step 4 — Security (see also `skills/core/SecurityAuditSkill.md`)
- [ ] No secrets, tokens, passwords, or connection strings hard-coded.
- [ ] No sensitive data (password, OTP, token, card number) written to logs.
- [ ] Input validated (Bean Validation `@Valid`, or explicit checks) at controller/service boundary.
- [ ] Authorization checked (RBAC / ownership) before mutating or returning data.
- [ ] Soft delete (`isDeleted` flag) used instead of hard `DELETE` where the entity is business data.

### Step 5 — Data & Persistence
- [ ] New queries reviewed for N+1 problems (use `@EntityGraph` / fetch joins when needed).
- [ ] Migration scripts (`templates/sql/`) accompany any entity/schema change.
- [ ] Indexes considered for new columns used in `WHERE`/`JOIN`.

### Step 6 — Testing
- [ ] Unit tests exist for new/changed service methods.
- [ ] Test coverage target ≥ 80% for the changed class (`rules/CODING_STANDARDS.md#testing-standards`).
- [ ] Tests cover both the happy path and at least one failure/edge case.
- [ ] No test depends on execution order or shared mutable static state.

### Step 7 — Readability & Maintainability
- [ ] Method length reasonable (~< 40 lines); extract private helpers if longer.
- [ ] No duplicated logic that already exists in a shared util/service.
- [ ] Magic numbers/strings replaced with named constants.
- [ ] Dead code / commented-out code removed.

---

## 3. Severity Levels for Review Comments

When leaving review feedback (or self-critiquing), tag each finding:

| Tag | Meaning | Action |
|-----|---------|--------|
| `[BLOCKER]` | Violates mandatory rule (security, standards, architecture) | Must fix before merge |
| `[MAJOR]` | Bug or design flaw likely to cause defects | Should fix before merge |
| `[MINOR]` | Style/readability nit, non-blocking | Fix if convenient |
| `[QUESTION]` | Needs clarification from author | Author responds |
| `[NIT]` | Pure style preference | Optional |

An agent must **not** report a task as complete if any `[BLOCKER]` remains open.

---

## 4. Self-Review Workflow For Agents

1. Generate the code change.
2. Re-read the diff top to bottom (do not skim).
3. Run through the checklist in Section 2, step by step — do not skip sections.
4. List every finding using the severity tags in Section 3.
5. Fix all `[BLOCKER]` and `[MAJOR]` findings.
6. Only then present the result to the user/tech lead, including a short summary of what was checked.

---

## 5. Example Review Output Format

```
## Code Review — UserService.java

[BLOCKER] Missing @Transactional on updateUser() — partial writes possible on failure.
[MAJOR] No null-check on request.getEmail() before duplicate lookup — NPE risk.
[MINOR] Variable name `u` should be `user` for clarity.
[QUESTION] Should deleted users still be searchable by admin? Business doc doesn't specify.

Summary: 1 blocker, 1 major, 1 minor, 1 question. Must resolve blocker + major before merge.
```

---

## 6. Related References

- `rules/CODING_STANDARDS.md` — formatting, JavaDoc, naming.
- `rules/ARCHITECTURE_RULES.md` — layering, transactions, error handling.
- `skills/core/SecurityAuditSkill.md` — deep-dive security checks.
- `skills/core/TestingSkill.md` — how to write/evaluate tests.
- `templates/java/ServiceTemplate.java`, `templates/java/ControllerTemplate.java` — the "gold standard" shape reviewed code should match.
