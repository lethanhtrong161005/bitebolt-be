# 📐 BiteBolt Coding Standards (MANDATORY)

**Status**: MANDATORY | **Version**: 2.0 | **Last Updated**: August 2024

> ⚠️ **These rules are NON-NEGOTIABLE**. Any code violating these standards will be rejected in code review.

---

## 📌 Table of Contents
1. [Google Java Style Guide](#google-java-style-guide)
2. [Indentation & Formatting](#indentation--formatting)
3. [JavaDoc Requirements](#javadoc-requirements)
4. [Naming Conventions](#naming-conventions)
5. [Code Comments (English Only)](#code-comments-english-only)
6. [Logging & Debugging](#logging--debugging)
7. [Exception Handling](#exception-handling)
8. [Data Access (JPA/Repositories)](#data-access-jparepositories)
9. [Security & Data Privacy](#security--data-privacy)
10. [Testing Standards](#testing-standards)
11. [Banned Practices](#banned-practices)

---

## 1. Google Java Style Guide

BiteBolt follows **Google Java Style Guide** (2017) with the following key rules:

### File Structure
```
1. License or copyright notice (if applicable)
2. Package statement
3. Import statements (grouped)
4. Class or interface declaration with JavaDoc
```

### Class Structure
```java
public class MyClass {
  // Constants
  private static final String CONSTANT_NAME = "value";
  
  // Fields
  private String instanceField;
  
  // Constructors
  public MyClass() { }
  
  // Methods (public first, then protected, then private)
  public void publicMethod() { }
  protected void protectedMethod() { }
  private void privateMethod() { }
}
```

---

## 2. Indentation & Formatting

### ✅ Space Rules
- **Indentation**: **2 spaces** (NOT tabs, NOT 4 spaces)
- **Tab size**: **2** (if you must use tabs for alignment)
- **Line length**: **100 characters** (flexible for URLs/docs)
- **Blank lines**: Max 1 consecutive blank line
- **No trailing spaces**: Always clean trailing whitespace

### ✅ Brace Style (Allman-style for classes, K&R for methods)

```java
// ✅ CORRECT: Class braces on new line
public class MyClass
{
  // ✅ CORRECT: Method braces on same line
  public void myMethod() {
    if (condition) {
      doSomething();
    }
  }
}

// ❌ WRONG
public class MyClass {
}
```

### ✅ Method Chains
```java
// ✅ CORRECT: One method per line for long chains
User user = userBuilder
    .withEmail("test@bitebolt.com")
    .withName("John Doe")
    .withRole(UserRole.CUSTOMER)
    .build();
```

### ✅ Long Parameter Lists
```java
// ✅ CORRECT: Break parameters across lines
public UserResponse createUser(
    String email,
    String password,
    String name,
    UserRole role
) {
  // implementation
}
```

---

## 3. JavaDoc Requirements

### ✅ Mandatory JavaDoc for:
1. **All public classes**
2. **All public methods**
3. **All public constants**
4. **All protected methods**
5. **All fields marked as important**

### ✅ JavaDoc Format

#### For Classes:
```java
/**
 * Brief description of what this class does (one line).
 * 
 * Longer explanation of the class purpose, responsibilities,
 * and key behaviors. Mention important constraints or design decisions.
 * 
 * Usage example:
 * <pre>
 *   UserService userService = new UserService(userRepository);
 *   UserResponse response = userService.createUser(request);
 * </pre>
 * 
 * Thread-safe: [Yes/No]
 * 
 * @see UserRepository
 * @see UserRequest
 */
public class UserService {
  // ...
}
```

#### For Methods:
```java
/**
 * Brief description of what this method does.
 * 
 * More detailed explanation if needed. Mention edge cases,
 * performance characteristics, or important side effects.
 * 
 * Step-by-step flow:
 * 1. Validate input parameters
 * 2. Check database for existing record
 * 3. Perform business logic
 * 4. Return result
 * 
 * @param email The email address to search for. Must be non-null and valid.
 * @param includeDeleted Whether to include soft-deleted users in search.
 * @return Optional containing the user if found, otherwise empty.
 * @throws InvalidEmailException if email format is invalid.
 * @throws DatabaseAccessException if database operation fails.
 */
public Optional<User> findByEmail(String email, boolean includeDeleted) 
    throws InvalidEmailException, DatabaseAccessException {
  // implementation
}
```

#### For Constants:
```java
/**
 * <ol>
 *   <li><b>VI:</b> Mã OTP đã hết hạn hoặc phiên không hợp lệ.</li>
 *   <li><b>EN:</b> OTP code has expired or session is invalid.</li>
 * </ol>
 */
public static final String ERROR_OTP_EXPIRED = "ERROR_OTP_EXPIRED";
```

#### For Fields:
```java
/**
 * Indicates whether this user has been soft-deleted.
 * When true, user should not appear in active user queries.
 * Deletion timestamp is stored in {@link #deletedAt}.
 */
private Boolean isDeleted = false;

/**
 * User's hashed password using BCrypt algorithm.
 * Should NEVER be logged or exposed in responses.
 */
private String hashedPassword;
```

### ❌ Bad JavaDoc Examples
```java
// ❌ WRONG: No JavaDoc
public String getUserName() {
  return userName;
}

// ❌ WRONG: Incomplete JavaDoc (missing @param, @return)
/**
 * Get user by ID
 */
public User getUser(Long userId) {
  // ...
}

// ❌ WRONG: Generic, unhelpful JavaDoc
/**
 * This method does something.
 */
public void process(String data) {
  // ...
}
```

---

## 4. Naming Conventions

### ✅ Class Names
- **PascalCase**: `UserService`, `UserRepository`, `CreateUserRequest`
- **Suffixes**: Use meaningful suffixes
  - Service classes: `*Service` (e.g., `UserService`)
  - Data transfer objects: `*Request`, `*Response`, `*DTO`
  - Repository classes: `*Repository` (e.g., `UserRepository`)
  - Exception classes: `*Exception` (e.g., `UserNotFoundException`)
  - Test classes: `*Test` (e.g., `UserServiceTest`)

### ✅ Variable & Method Names
- **camelCase**: `userId`, `userEmail`, `isActive`
- **Boolean prefixes**: `is*`, `has*`, `can*`
  - `isDeleted`, `isActive`, `hasPermission`, `canAccess`
- **Avoid**: Single letter variables (except loop counters)

### ✅ Constant Names
- **UPPER_SNAKE_CASE**: `MAX_USER_NAME_LENGTH`, `DEFAULT_TIMEOUT_MS`
- **Group related constants**: Organize in same class or constant file

### ✅ Package Names
- **Lowercase with dots**: `com.bitebolt.user.service`
- **Never use underscores** or hyphens
- **Meaningful hierarchy**: `domain.layer.feature`

### ✅ Examples
```java
// ✅ CORRECT
public class UserService {
  private static final int MAX_PASSWORD_LENGTH = 128;
  private static final String INVALID_EMAIL_MESSAGE = "Email format is invalid";
  
  private UserRepository userRepository;
  private boolean isInitialized;
  private List<User> activeUsers;
  
  public UserResponse createUser(CreateUserRequest request) { }
  public void deleteUser(Long userId) { }
  public boolean hasAdminRole(User user) { }
}

// ❌ WRONG
public class user_service {                    // Wrong: not PascalCase
  private static final int pwd_max = 128;      // Wrong: not UPPER_SNAKE_CASE
  private UserRepository ur;                   // Wrong: cryptic abbreviation
  private boolean init;                        // Wrong: not clear what it means
  
  public void process(String x) { }            // Wrong: single letter param
  public void deleteit(Long id) { }            // Wrong: poor naming
}
```

---

## 5. Code Comments (English Only)

### ✅ Comment Language
- **ONLY English** in code comments
- **NEVER Vietnamese** or any other language
- Use clear, professional English

### ✅ Comment Types

#### Block Comments (Algorithm explanation)
```java
/**
 * Calculate user score based on multiple factors.
 * The algorithm:
 * 1. Start with base score (0)
 * 2. Add points for completed orders
 * 3. Multiply by loyalty factor
 * 4. Apply caps (min=0, max=1000)
 */
private int calculateScore(User user) {
  int baseScore = 0;
  // Add points for each completed order (10 points per order)
  baseScore += user.getCompletedOrders().size() * 10;
  
  // Apply loyalty multiplier (more orders = higher multiplier)
  double loyaltyFactor = Math.min(1.5, 1.0 + (user.getCompletedOrders().size() * 0.1));
  
  return (int) Math.min(1000, baseScore * loyaltyFactor);
}
```

#### Inline Comments (Why, not what)
```java
// ✅ CORRECT: Explains WHY
// User must be verified via email before accessing advanced features
if (!user.isEmailVerified()) {
  throw new UnverifiedException("EMAIL_NOT_VERIFIED");
}

// ✅ CORRECT: Explains non-obvious logic
// We cache the result for 1 hour because the user's role rarely changes
// and role lookups are expensive database queries
return cache.getOrElse("role:" + userId, () -> loadRoleFromDatabase(userId), Duration.ofHours(1));

// ❌ WRONG: States the obvious
int age = user.getAge();  // Get user's age
if (age > 18) {           // Check if age is greater than 18
  allowAccess();
}
```

#### TODO Comments
```java
// TODO: Implement email verification after auth-service is ready
// See: docs/security/auth-plans/PHASE2_EXTERNAL_AUTH_PLAN.md
```

### ❌ Banned Comments
```java
// ❌ WRONG: Contains sensitive data
// Login attempt with password: mypassword123
log.info("Login attempt processed");

// ❌ WRONG: Contains OTP
// OTP verification code: 123456
log.info("OTP verification completed");

// ❌ WRONG: Vietnamese
// Kiểm tra xem người dùng có phải là admin
if (user.isAdmin()) { }

// ❌ WRONG: Obvious comment
int id = user.getId();  // Get the ID
```

---

## 6. Logging & Debugging

### ✅ Logging Rules
1. **Use SLF4J** (`org.slf4j.Logger`)
2. **NEVER use** `System.out.println()`, `System.err.println()`
3. **Appropriate log levels**:
   - `TRACE`: Very detailed information
   - `DEBUG`: Detailed technical information
   - `INFO`: Business events (user login, order created)
   - `WARN`: Potentially harmful situations
   - `ERROR`: Error events that might still allow the app to run

### ✅ Logging Examples
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
  private static final Logger logger = LoggerFactory.getLogger(UserService.class);
  
  public void processUser(User user) {
    logger.debug("Processing user: {}", user.getId());
    
    // Business logic
    if (processSuccessful) {
      logger.info("User {} successfully processed", user.getId());  // No sensitive data
    } else {
      logger.error("Failed to process user: {}", user.getId());
    }
  }
  
  public void handleException(Exception e) {
    logger.error("An error occurred during processing", e);  // Include stack trace
  }
}
```

### ❌ Logging Violations
```java
// ❌ WRONG: System.out
System.out.println("Debug: user=" + user);

// ❌ WRONG: Logging password
logger.info("User password: {}", password);

// ❌ WRONG: Logging OTP
logger.info("OTP code: {}", otp);

// ❌ WRONG: Logging credit card
logger.info("Card number: {}", creditCard);

// ❌ WRONG: No logger instance
log.info("Message");  // 'log' is not defined
```

---

## 7. Exception Handling

### ✅ Exception Rules
1. **Create custom exceptions** for business errors
2. **Extend proper base class**:
   - Business exceptions → `RuntimeException` or `HttpException`
   - Never extend `Exception` directly (forces checked exception handling)
3. **Always provide message constants**
4. **Log exceptions with context**

### ✅ Exception Example
```java
/**
 * Thrown when a user with the given email already exists in the system.
 * This prevents duplicate accounts and helps maintain data integrity.
 * 
 * HTTP Status: 409 Conflict
 */
public class UserAlreadyExistsException extends HttpException {
  
  public UserAlreadyExistsException(String messageCode) {
    super(HttpStatus.CONFLICT, messageCode);
  }
  
  public UserAlreadyExistsException(String messageCode, Throwable cause) {
    super(HttpStatus.CONFLICT, messageCode, cause);
  }
}

// Usage
if (userRepository.existsByEmail(email)) {
  throw new UserAlreadyExistsException("EMAIL_ALREADY_REGISTERED");
}
```

### ✅ Try-Catch Best Practices
```java
// ✅ CORRECT: Handle specific exceptions
try {
  user = userRepository.save(user);
} catch (DataIntegrityViolationException e) {
  logger.error("Failed to save user due to constraint violation", e);
  throw new UserAlreadyExistsException("EMAIL_ALREADY_REGISTERED", e);
} catch (Exception e) {
  logger.error("Unexpected error while saving user", e);
  throw new SystemException("SYSTEM_ERROR", e);
}

// ❌ WRONG: Catching all exceptions silently
try {
  userRepository.save(user);
} catch (Exception e) {
  // Silently ignore errors
}

// ❌ WRONG: Generic exception message
catch (Exception e) {
  throw new Exception("Error");
}
```

---

## 8. Data Access (JPA/Repositories) & Common Pagination

### ✅ Query & Pagination Rules
1. **ALWAYS use** `@Query` with JPQL for filtering `isDeleted`
2. **NEVER rely** on Spring Data method name derivation for soft deletes
3. **Include JPA Annotations** for entity mapping
4. **ALWAYS extend `BaseRequestParam`** (`com.bitebolt.common.dto.request.BaseRequestParam`) for search/pagination filter DTOs.
5. **ALWAYS return `PageResponse<T>`** (`com.bitebolt.common.dto.response.PageResponse`) instead of raw Spring Data `Page<T>` in Service and Controller layers.
6. **ALWAYS use Spring `@Component` Helper/Mapper classes** (e.g. `AuditLogHelper`) to map Entities to DTOs instead of manual mapping methods inside Service implementations.
7. **USE `GenericSpecification<E>`** for dynamic query filtering with Criteria API.

### ✅ Repository Example
```java
public interface UserRepository extends JpaRepository<User, Long> {
  
  /**
   * Find user by email, excluding soft-deleted records.
   * 
   * @param email The email to search for
   * @return Optional containing user if found
   */
  @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.isDeleted = false")
  Optional<User> findByEmailActive(String email);
  
  /**
   * Find all active users with pagination.
   * Excludes soft-deleted users automatically.
   * 
   * @param pageable Pagination parameters
   * @return Page of active users
   */
  @Query("SELECT u FROM User u WHERE u.isDeleted = false")
  Page<User> findAllActive(Pageable pageable);
  
  /**
   * Count active users in the system.
   * 
   * @return Number of non-deleted users
   */
  @Query("SELECT COUNT(u) FROM User u WHERE u.isDeleted = false")
  long countActive();
  
  /**
   * Soft delete a user by setting isDeleted flag.
   * Does NOT physically remove from database.
   * 
   * @param userId ID of user to delete
   * @return Number of records updated
   */
  @Modifying
  @Query("UPDATE User u SET u.isDeleted = true WHERE u.id = ?1")
  int softDeleteById(Long userId);
}
```

### ✅ Entity Example
```java
/**
 * Represents a BiteBolt user account.
 * Supports role-based access control with soft delete capability.
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_deleted", columnList = "is_deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  
  /**
   * Unique identifier for this user.
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  /**
   * User's email address (unique, required).
   */
  @Column(nullable = false, unique = true, length = 255)
  private String email;
  
  /**
   * BCrypt-hashed password. Should never be exposed in responses.
   */
  @Column(nullable = false, length = 255)
  private String password;
  
  /**
   * Indicates whether this record is soft-deleted.
   * When true, user should not appear in active user queries.
   */
  @Column(nullable = false, name = "is_deleted")
  private Boolean isDeleted = false;
}
```

---

## 9. Security & Data Privacy

### ✅ Security Rules
1. **NEVER log sensitive data**: Passwords, tokens, OTPs, credit cards
2. **Use masking utilities** when logging must include user data
3. **Validate ALL inputs** before using
4. **Use BCrypt** for password hashing
5. **Sanitize** user inputs in SQL queries

### ✅ Security Examples
```java
/**
 * Validates user input before database operations.
 */
private void validateUserInput(CreateUserRequest request) throws ValidationException {
  if (request == null || request.getEmail() == null || request.getEmail().isEmpty()) {
    throw new ValidationException("EMAIL_REQUIRED");
  }
  
  if (!isValidEmail(request.getEmail())) {
    throw new ValidationException("EMAIL_INVALID_FORMAT");
  }
  
  if (request.getPassword() == null || request.getPassword().length() < 8) {
    throw new ValidationException("PASSWORD_TOO_SHORT");
  }
}

/**
 * Masks sensitive user data before logging.
 */
public void logUserAction(User user, String action) {
  String maskedEmail = maskEmail(user.getEmail());
  logger.info("User action - email: {}, action: {}", maskedEmail, action);
}

/**
 * Hashes password using BCrypt.
 */
@Autowired
private PasswordEncoder passwordEncoder;  // BCryptPasswordEncoder bean

public void saveUser(User user, String plainPassword) {
  user.setPassword(passwordEncoder.encode(plainPassword));
  userRepository.save(user);
  // Note: We do NOT log the plainPassword
}
```

### ❌ Security Violations
```java
// ❌ WRONG: Logging password
logger.info("User logged in with password: {}", password);

// ❌ WRONG: Logging OTP
logger.info("OTP verification: {} for user {}", otp, userId);

// ❌ WRONG: Storing plain password
user.setPassword(plainPassword);  // Should be hashed!

// ❌ WRONG: SQL injection risk
String query = "SELECT * FROM users WHERE email = '" + email + "'";
resultSet = statement.executeQuery(query);

// ❌ WRONG: No input validation
public void deleteUser(Long userId) {
  userRepository.deleteById(userId);  // What if userId is invalid?
}
```

---

## 10. Testing Standards

### ✅ Testing Rules
1. **Minimum 80% code coverage** for all services
2. **Unit tests** for business logic
3. **Integration tests** for repository/database access
4. **Test should be independent** and not rely on execution order
5. **Use meaningful test names** that describe what is being tested

### ✅ Test Example
```java
/**
 * Unit tests for UserService.
 * Tests business logic and error conditions.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  
  @Mock
  private UserRepository userRepository;
  
  @Mock
  private PasswordEncoder passwordEncoder;
  
  @InjectMocks
  private UserService userService;
  
  @Test
  void testCreateUser_WithValidInput_ShouldReturnUserResponse() {
    // Arrange
    CreateUserRequest request = CreateUserRequest.builder()
        .email("test@bitebolt.com")
        .password("securePassword123")
        .name("John Doe")
        .build();
    
    User savedUser = User.builder()
        .id(1L)
        .email("test@bitebolt.com")
        .name("John Doe")
        .isDeleted(false)
        .build();
    
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);
    
    // Act
    UserResponse response = userService.createUser(request);
    
    // Assert
    assertNotNull(response);
    assertEquals("test@bitebolt.com", response.getEmail());
    assertEquals("John Doe", response.getName());
    verify(userRepository, times(1)).save(any(User.class));
  }
  
  @Test
  void testCreateUser_WithDuplicateEmail_ShouldThrowException() {
    // Arrange
    CreateUserRequest request = CreateUserRequest.builder()
        .email("existing@bitebolt.com")
        .password("securePassword123")
        .build();
    
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);
    
    // Act & Assert
    assertThrows(UserAlreadyExistsException.class, () -> {
      userService.createUser(request);
    });
  }
}
```

---

## 11. Banned Practices

### ❌ NEVER DO THIS

| What | Why | Correct Alternative |
|------|-----|-----|
| `System.out.println()` | Not captured by logging framework | Use `Logger.info()` |
| `e.printStackTrace()` | Unstructured error reporting | Use `logger.error("msg", e)` |
| Catch generic `Exception` | Masks actual errors | Catch specific exceptions |
| Silent exception catching | Hides errors | Log and re-throw or handle properly |
| SQL string concatenation | SQL injection vulnerability | Use JPA @Query with parameters |
| Magic numbers | Unclear business logic | Extract to named constants |
| Single-letter variables | Confusing and unmaintainable | Use descriptive names |
| Comment-heavy code | Indicates code is unclear | Refactor code to be self-documenting |
| No null checks | NullPointerException risks | Use Optional or explicit null checks |
| Mutable global state | Hard to debug and test | Use dependency injection |

---

## 📋 Pre-Commit Checklist

Before committing code, verify:

- [ ] Follows Google Java Style (2 spaces indentation)
- [ ] All public classes have JavaDoc
- [ ] All public methods have @param, @return, @throws documentation
- [ ] Variable names are camelCase
- [ ] Constant names are UPPER_SNAKE_CASE
- [ ] All comments are in English
- [ ] No `System.out.println()` or `printStackTrace()`
- [ ] No logging of sensitive data (password, OTP, token)
- [ ] Uses JPA @Query instead of raw SQL
- [ ] Exceptions are handled with try-catch where needed
- [ ] Test coverage is ≥80%
- [ ] No trailing spaces or unused imports
- [ ] `mvn spotless:check` passes
- [ ] `mvn checkstyle:check` passes

---

## 📞 Questions?

If you have questions about these standards:
1. Check examples in `.agents/examples/`
2. Review templates in `.agents/templates/`
3. Refer to specific skills in `.agents/skills/`
4. Ask your Tech Lead

---

**Effective Date**: August 2024  
**Version**: 2.0  
**Status**: MANDATORY  

