# 🌿 Branch Naming Rules (MANDATORY)

```
<type>/<ticket-id>-<short-kebab-case-description>
```

## Allowed Types
`feature`, `fix`, `hotfix`, `refactor`, `chore`, `docs`

## Examples
```
feature/BB-102-refund-status-flow
fix/BB-118-jwt-expiry-check
hotfix/BB-201-payment-timeout
chore/BB-050-upgrade-spring-boot-3
```

## Rules
- Always branch off the latest `main`/`develop` per team convention.
- Kebab-case, English only, no spaces or underscores.
- Keep the description short (≤ 6 words) — full context goes in the PR description, not the branch name.
