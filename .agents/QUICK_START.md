# 🚀 Quick Start Guide for Agents

Este é um guia **prático** para agentes de IA começarem a trabalhar em tarefas no projeto BiteBolt.

## 1️⃣ Antes de Começar Qualquer Tarefa

### 📋 Checklist Inicial (5 minutos)
- [ ] Leia o arquivo `.agents/rules/CODING_STANDARDS.md` (**MANDATORY**)
- [ ] Entenda a arquitetura em `.agents/docs/business/AUTH_BUSINESS_DESIGN.md`
- [ ] Escolha o template correto em `.agents/templates/java/`
- [ ] Procure skills relevantes em `.agents/skills/`

### 🔍 Determine Sua Tarefa
| Tarefa | Vá Para |
|--------|---------|
| Criar novo Service | `prompts/code-generation/java-service.prompt` |
| Criar novo Controller | `prompts/code-generation/java-controller.prompt` |
| Revisar segurança do código | `prompts/code-review/security-review.prompt` |
| Implementar autenticação | `docs/security/auth-plans/PHASE1_INTERNAL_SSO_PLAN.md` |
| Configurar logging | `docs/observability/audit-logging/` |
| Integrar com gRPC | `skills/microservices/gRPCImplementationSkill.md` |
| Escrever testes | `templates/java/TestTemplate.java` |

---

## 2️⃣ Padrões Obrigatórios (MUST FOLLOW)

### ✅ Coding Format
```
✓ Google Java Style Guide
✓ Indentation: 2 spaces (NOT tabs)
✓ Tab width: 2
✓ Line length: 100 characters max (flexible for URLs)
✓ NO trailing spaces
```

### ✅ JavaDoc Obrigatório
```java
/**
 * Descrição breve do que a classe faz.
 * 
 * Explicação mais detalhada se necessário.
 * Use casos de uso:
 * - Caso 1
 * - Caso 2
 */
public class MyClass {
  
  /**
   * Brief method description.
   * 
   * @param param1 Description of param1
   * @param param2 Description of param2
   * @return Description of return value
   * @throws CustomException Description of when this exception is thrown
   */
  public String myMethod(String param1, int param2) throws CustomException {
    // Step 1: Validate inputs
    // Step 2: Process logic
    // Step 3: Return result
  }
}
```

### ✅ Comentários ONLY em INGLÊS
```java
// ✅ CORRETO
// Retrieve user by ID from database
User user = userRepository.findById(userId);

// ❌ ERRADO
// Buscar usuário por ID no banco de dados
User user = userRepository.findById(userId);
```

### ✅ NUNCA faça isto
```java
// ❌ Senhas em logs
log.info("User login with password: " + password);

// ❌ OTP em logs
log.info("OTP sent: " + otp);

// ❌ System.out
System.out.println("Debug message");  // Use Logger instead!

// ❌ SQL direto
String sql = "SELECT * FROM users WHERE id = ?";
ResultSet rs = statement.executeQuery(sql);

// ❌ Sem tratamento de exceção
userRepository.delete(user);  // What if it fails?

// ❌ Magic numbers
if (user.getAge() > 18) { ... }  // Should be a constant
```

---

## 3️⃣ Template-First Approach

**SEMPRE** comece copiando um template apropriado:

### Para novo Service:
```bash
cp .agents/templates/java/ServiceTemplate.java src/main/java/com/bitebolt/myservice/service/MyService.java
```

### Para novo Controller:
```bash
cp .agents/templates/java/ControllerTemplate.java src/main/java/com/bitebolt/myapi/controller/MyController.java
```

### Para novo Repository:
```bash
cp .agents/templates/java/RepositoryTemplate.java src/main/java/com/bitebolt/myservice/repository/MyRepository.java
```

---

## 4️⃣ Referência de Arquitetura

BiteBolt segue um **Maven multi-module monorepo** com:

```
bitebolt-backend/
├── common-libraries/
│   ├── common-dto/          # Shared data structures
│   ├── common-validation/   # Validation annotations
│   ├── common-exception/    # Global exception handling
│   ├── common-logging/      # MDC & tracing
│   ├── common-grpc/         # gRPC stubs
│   └── common-security/     # JWT & cryptography
├── core-services/
│   ├── auth-service/        # Authentication & Authorization
│   ├── user-service/        # User management
│   ├── booking-service/     # Booking logic
│   └── audit-service/       # Audit trail storage
├── api-gateway/             # Central security gateway
└── .agents/                 # THIS FOLDER
```

### Comunicação Entre Serviços
- **Intra-service**: Use JPA/SQL
- **Inter-service**: Use **gRPC** (NEVER use REST for internal calls)
- **Async events**: Use **Kafka**
- **Cache**: Use **Redis**

---

## 5️⃣ Exemplo Prático: Criar um novo User Service

### Passo 1: Copie o template
```bash
cp .agents/templates/java/ServiceTemplate.java UserService.java
```

### Passo 2: Preencha com sua lógica
```java
/**
 * Service layer for user management operations.
 * Handles business logic for user creation, updates, and deletions.
 * Uses soft delete pattern with isDeleted flag.
 * 
 * Depends on:
 * - UserRepository for database access
 * - PasswordEncoder for secure password handling
 * - UserMapper for DTO conversions
 */
@Service
@RequiredArgsConstructor
public class UserService {
  
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  
  /**
   * Create a new user in the system.
   * 
   * @param createUserRequest DTO containing user details
   * @return UserResponse containing created user info
   * @throws UserAlreadyExistsException if user email already exists
   */
  public UserResponse createUser(CreateUserRequest createUserRequest) 
      throws UserAlreadyExistsException {
    // Step 1: Validate request input
    validateUserInput(createUserRequest);
    
    // Step 2: Check if user already exists
    if (userRepository.existsByEmail(createUserRequest.getEmail())) {
      throw new UserAlreadyExistsException("EMAIL_ALREADY_REGISTERED");
    }
    
    // Step 3: Encode password
    String encodedPassword = passwordEncoder.encode(createUserRequest.getPassword());
    
    // Step 4: Create user entity
    User user = User.builder()
        .email(createUserRequest.getEmail())
        .password(encodedPassword)
        .name(createUserRequest.getName())
        .isDeleted(false)
        .build();
    
    // Step 5: Save and return
    User savedUser = userRepository.save(user);
    return userMapper.toResponse(savedUser);
  }
}
```

### Passo 3: Siga o checklist
- [ ] Todas as classes têm JavaDoc
- [ ] Todos os métodos públicos têm @param, @return, @throws
- [ ] Usa 2 spaces para indentação
- [ ] Sem dados sensíveis em logs
- [ ] Trata exceções corretamente
- [ ] Usa JPA @Query em vez de SQL direto

---

## 6️⃣ Validação Antes de Commitar

Rode estas verificações:

```bash
# 1. Verificar formatação (Google Style)
mvn spotless:check

# 2. Verificar bugs potenciais
mvn spotbugs:check

# 3. Verificar cobertura de testes (>80%)
mvn jacoco:report

# 4. Verificar violações de padrão
mvn checkstyle:check
```

---

## 7️⃣ Exemplo de Resposta

Quando gerar código, sempre forneça em este formato:

```
✅ TASK COMPLETED

📁 Files Created/Modified:
- src/main/java/com/bitebolt/user/service/UserService.java (NEW)
- src/main/java/com/bitebolt/user/controller/UserController.java (MODIFIED)
- src/test/java/com/bitebolt/user/service/UserServiceTest.java (NEW)

📋 Summary:
- Implemented UserService with CRUD operations
- Added security annotations for authorization
- Wrote unit tests with 92% code coverage
- All code follows Google Java Style (2 spaces)
- All classes have JavaDoc documentation

⚠️ Important Notes:
- User deletion uses soft delete (isDeleted = true)
- Password hashing uses BCrypt encoder
- All user endpoints require ADMIN or STAFF role
- Sensitive data (password, OTP) never logged

🔗 Related Docs:
- See docs/business/user-management-plan.md for business rules
- See docs/security/API_GATEWAY_SECURITY_PLAN.md for authentication flow
```

---

## 8️⃣ Quando Estiver Preso

| Problema | Solução |
|----------|---------|
| "Como estruturo um Service?" | Veja `templates/java/ServiceTemplate.java` |
| "Qual é o padrão de exceção?" | Veja `templates/java/ExceptionTemplate.java` |
| "Como faço queries JPA?" | Veja `examples/repository/UserRepositoryExample.java` |
| "Como estruturo testes?" | Veja `templates/java/TestTemplate.java` |
| "Como implemento autenticação?" | Veja `docs/security/auth-plans/PHASE1_INTERNAL_SSO_PLAN.md` |
| "Como configuro logging?" | Veja `docs/observability/audit-logging/audit-logging-guide.md` |

---

## 🎯 Resumo Executivo

| O QUÊ | COMO | ONDE |
|-------|------|------|
| Entender regras | Leia CODING_STANDARDS.md | rules/ |
| Ver exemplos | Copie templates | templates/ |
| Aprender arquitetura | Leia business docs | docs/business/ |
| Usar habilidades | Consulte skills | skills/ |
| Gerar código | Use prompts | prompts/code-generation/ |
| Revisar código | Use review prompts | prompts/code-review/ |

---

## ✨ Dicas Finais

1. **Leia Primeiro, Código Depois**: Sempre leia a documentação relevante antes de começar
2. **Template-First**: NUNCA comece do zero, sempre copie um template
3. **JavaDoc is Sacred**: Todo código público PRECISA de documentação
4. **Tests are Non-Negotiable**: Sem testes, seu PR será rejeitado
5. **Ask Questions**: Melhor perguntar que fazer errado
6. **Follow the Checklist**: Não pule nenhum item da checklist

---

**Last Updated**: August 2024  
**Framework Version**: 2.0 (Enterprise Grade)

