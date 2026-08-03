# 🧪 Testing Skill

**Category**: Core | **Use When**: Writing or reviewing unit/integration tests, or when a service/method change needs coverage.

---

## 1. Test Levels
| Level | Tool | Scope |
|-------|------|-------|
| Unit | JUnit 5 + Mockito | Single class, dependencies mocked |
| Integration | `@SpringBootTest` + Testcontainers | Real DB/Kafka in a container |
| Contract | gRPC/REST contract tests | Verify API shape between services |

## 2. Step-by-Step: Writing a Unit Test
1. Name the class `<ClassUnderTest>Test`, mirror the package of the class under test.
2. Structure each test method as **Given / When / Then** (Arrange / Act / Assert), one behavior per test.
3. Mock all collaborators (`@Mock`) — a service unit test never hits a real DB.
4. Cover: happy path, at least one validation failure, at least one "not found" case, and any branch with distinct business meaning.
5. Assert both the return value **and** any expected side effect (e.g., `verify(repository).save(any())`).
6. Never assert on log output or private fields via reflection.

## 3. Naming Convention
```
methodName_condition_expectedResult()

// Example
createUser_whenEmailAlreadyExists_throwsDuplicateEmailException()
```

## 4. Minimum Coverage Bar
- New/changed service methods: **≥ 80%** line coverage (`rules/CODING_STANDARDS.md`).
- Every public method must have at least one test exercising it.
- `mvn test` and `mvn jacoco:report` must pass before a change is considered done.

## 5. Anti-Patterns
- Tests that depend on execution order.
- Tests with no assertions ("smoke test that just calls the method").
- Overuse of `@SpringBootTest` for pure unit logic (slow, unnecessary — prefer plain JUnit + Mockito).
- Sleep-based waits in async tests — use `Awaitility` instead.

## 6. Related References
- `rules/CODING_STANDARDS.md#testing-standards`
- `skills/core/CodeReviewSkill.md` (Step 6 — Testing)
- `templates/java/` (add `TestTemplate.java` here when created)
