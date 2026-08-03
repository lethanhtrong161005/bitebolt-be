# 📝 Commit Message Rules (MANDATORY)

Follow **Conventional Commits**:

```
<type>(<scope>): <short summary, imperative mood, English only>

[optional body — why the change was made, not what]

[optional footer — BREAKING CHANGE:, Refs: TICKET-123]
```

## Allowed Types
| Type | Use For |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `refactor` | Code change that neither fixes a bug nor adds a feature |
| `test` | Adding/updating tests only |
| `docs` | Documentation only |
| `chore` | Build, tooling, dependency updates |
| `perf` | Performance improvement |
| `security` | Security-related fix |

## Rules
- Subject line ≤ 72 characters, no trailing period.
- Written in English, imperative mood ("add", not "added"/"adds").
- One logical change per commit — do not mix unrelated changes.
- Reference the related ticket/issue in the footer when applicable.

## Examples
```
feat(order-service): add refund status transition validation

fix(auth): prevent expired JWT from passing gateway filter

refactor(user-service): extract email validation into shared util
```
