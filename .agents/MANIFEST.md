# 📦 BiteBolt Agents Framework - Complete Manifest

**Version**: 2.0 (Enterprise Grade)  
**Last Updated**: August 2024  
**Status**: Production Ready

---

## 📁 Complete Directory Structure

```
.agents/
│
├── README.md                          # Framework overview & quick reference
├── QUICK_START.md                     # Practical quick start guide for agents
├── MANIFEST.md                        # This file - complete contents listing
│
├── docs/                              # Business & Architecture Documentation
│   ├── business/                      # Business domain knowledge
│   │   ├── AUTH_BUSINESS_DESIGN.md                    # Authentication architecture
│   │   ├── user-management-plan.md                   # User management specifications
│   │   └── frontend-architecture/
│   │       └── FRONTEND_ARCHITECTURE_PLAN.md         # Frontend integration patterns
│   ├── security/                      # Security & Authentication
│   │   ├── API_GATEWAY_SECURITY_PLAN.md              # API Gateway security model
│   │   ├── USER_CONTEXT_PLAN.md                      # User context propagation
│   │   └── auth-plans/
│   │       ├── PHASE1_INTERNAL_SSO_PLAN.md           # Microsoft Entra ID SSO (ADMIN/STAFF)
│   │       ├── PHASE2_EXTERNAL_AUTH_PLAN.md          # Phone+OTP & Google login (DRIVER/RIDER)
│   │       └── implementation_plan_logout.md         # Token blacklist & logout
│   └── observability/                 # Logging & Monitoring
│       └── audit-logging/
│           ├── audit-logging-guide.md                # Distributed audit logging setup
│           └── implementation-plan.md                # Implementation roadmap
│
├── rules/                             # Mandatory Rules & Standards
│   ├── CODING_STANDARDS.md            # 🔴 MANDATORY - Google Java Style, JavaDoc requirements
│   ├── ARCHITECTURE_RULES.md          # 🔴 MANDATORY - Microservices patterns, DDD principles
│   ├── SECURITY_RULES.md              # Security & data privacy requirements (TO BE CREATED)
│   ├── NAMING_CONVENTIONS.md          # Naming standards (TO BE CREATED)
│   └── git/
│       ├── COMMIT_MESSAGE_RULES.md    # Git commit conventions (TO BE CREATED)
│       └── BRANCH_NAMING_RULES.md     # Branch naming patterns (TO BE CREATED)
│
├── skills/                            # Agent Skills & Capabilities
│   ├── README.md                      # Skills registry & index
│   ├── core/                          # Fundamental skills
│   │   ├── CodeAnalysisSkill.md                      # Code quality & patterns
│   │   ├── ArchitectureDesignSkill.md               # System design principles
│   │   └── SecurityAuditSkill.md                     # Security vulnerability detection
│   ├── microservices/                 # Inter-service communication
│   │   ├── gRPCImplementationSkill.md                # gRPC service development
│   │   ├── KafkaIntegrationSkill.md                  # Event streaming setup
│   │   └── RedisOptimizationSkill.md                 # Caching & session strategies
│   ├── frontend/                      # Frontend integration
│   │   ├── ReactComponentSkill.md                    # React best practices
│   │   └── UIUXConsistencySkill.md                   # Design system compliance
│   └── devops/                        # Deployment & Operations
│       ├── DockerContainerizationSkill.md            # Docker image creation
│       └── K8sDeploymentSkill.md                     # Kubernetes deployment
│
├── templates/                         # Code Templates (Use as starting points)
│   ├── java/
│   │   ├── ServiceTemplate.java                      # Service layer template
│   │   ├── ControllerTemplate.java                   # REST controller template
│   │   ├── RepositoryTemplate.java                   # JPA repository template (TO BE CREATED)
│   │   ├── DTOTemplate.java                          # Request/response DTO template (TO BE CREATED)
│   │   ├── EntityTemplate.java                       # JPA entity template (TO BE CREATED)
│   │   ├── ExceptionTemplate.java                    # Custom exception template (TO BE CREATED)
│   │   ├── TestTemplate.java                         # Unit test template (TO BE CREATED)
│   │   ├── ConfigTemplate.java                       # Spring configuration template (TO BE CREATED)
│   │   └── InterceptorTemplate.java                  # HTTP interceptor template (TO BE CREATED)
│   ├── xml/
│   │   ├── pom-dependency-template.xml               # Maven dependency snippet (TO BE CREATED)
│   │   └── spring-config-template.xml                # Spring configuration template (TO BE CREATED)
│   └── sql/
│       ├── migration-template.sql                    # Flyway migration template (TO BE CREATED)
│       └── seed-data-template.sql                    # Initial data template (TO BE CREATED)
│
├── prompts/                           # Structured Prompts for Agents
│   ├── system-prompt.md               # 🔴 MANDATORY - Main system prompt for all tasks
│   ├── code-generation/               # Code generation prompts
│   │   ├── java-service.prompt                       # Service implementation (TO BE CREATED)
│   │   ├── java-controller.prompt                    # Controller implementation (TO BE CREATED)
│   │   ├── java-repository.prompt                    # Repository implementation (TO BE CREATED)
│   │   ├── java-exception-handler.prompt             # Exception handler setup (TO BE CREATED)
│   │   └── java-test.prompt                          # Test implementation (TO BE CREATED)
│   ├── code-review/                   # Code review prompts
│   │   ├── security-review.prompt                    # Security vulnerability check
│   │   ├── performance-review.prompt                 # Performance & optimization
│   │   ├── test-coverage-review.prompt               # Test coverage validation
│   │   └── architecture-review.prompt                # Architecture compliance
│   ├── documentation/                 # Documentation prompts
│   │   ├── api-documentation.prompt                  # OpenAPI/Swagger generation
│   │   ├── architecture-doc.prompt                   # Architecture documentation
│   │   └── deployment-guide.prompt                   # Deployment instructions
│   └── analysis/                      # Analysis prompts
│       ├── root-cause-analysis.prompt                # Bug analysis
│       ├── technical-debt.prompt                     # Technical debt assessment
│       └── migration-planning.prompt                 # Migration strategy
│
├── tools/                             # Tools & Utilities
│   ├── code-formatter-config.md       # Google Style + 2 spaces configuration
│   ├── checkstyle-config.xml          # Static code analysis rules
│   ├── spotbugs-config.xml            # Bug detection configuration
│   └── analysis-scripts/
│       ├── measure-complexity.sh      # Cyclomatic complexity analyzer
│       └── validate-coverage.sh       # Test coverage validator
│
├── examples/                          # Reference Implementations
│   ├── service-implementation/
│   │   ├── UserServiceExample.md                     # Documented example
│   │   └── UserServiceExample.java                   # Implementation example
│   ├── api-endpoint/
│   │   ├── UserControllerExample.md                  # Documented example
│   │   └── UserControllerExample.java                # Implementation example
│   ├── repository/
│   │   ├── UserRepositoryExample.md                  # Documented example
│   │   └── UserRepositoryExample.java                # Implementation example
│   ├── exception-handling/
│   │   └── GlobalExceptionHandlerExample.java        # Exception handler example
│   ├── testing/
│   │   ├── ServiceUnitTestExample.java               # Unit test example
│   │   └── IntegrationTestExample.java               # Integration test example
│   └── microservice-communication/
│       ├── gRPCClientExample.java                    # gRPC client usage
│       └── KafkaProducerExample.java                 # Kafka event publishing
│
└── .gitkeep                           # Ensure empty folders tracked by git
```

---

## 📊 File Statistics

| Category | Count | Status |
|----------|-------|--------|
| Documentation (docs/) | 10 | ✅ Complete |
| Rules (rules/) | 2 | ✅ Complete (4 TO BE CREATED) |
| Skills (skills/) | 8 | ✅ Complete (6 TO BE CREATED) |
| Templates (templates/) | 2 | ✅ Complete (9 TO BE CREATED) |
| Prompts (prompts/) | 5 | ✅ Complete (8 TO BE CREATED) |
| Examples (examples/) | 8 | ✅ Complete |
| **TOTAL** | **35** | **15 Complete, 20 TO CREATE** |

---

## 🎯 Priority Order for Reading

### 👑 **CRITICAL - READ FIRST** (Mandatory for all agents)
1. `.agents/QUICK_START.md` (15 min)
2. `.agents/rules/CODING_STANDARDS.md` (30 min)
3. `.agents/prompts/system-prompt.md` (15 min)
4. `.agents/rules/ARCHITECTURE_RULES.md` (20 min)

**Time**: ~80 minutes

### 📖 **IMPORTANT** (Read before specific tasks)
1. Relevant business doc in `docs/business/` (task-dependent)
2. Relevant security doc in `docs/security/` (if auth-related)
3. Relevant skill in `skills/` (task-dependent)
4. Relevant template in `templates/java/` (task-dependent)

### 📚 **REFERENCE** (Look up as needed)
- Tools configuration in `tools/`
- Examples in `examples/`
- Code review prompts in `prompts/code-review/`

---

## 🔄 File Update Frequency

| Folder | Frequency | Who Updates |
|--------|-----------|-------------|
| `docs/` | As business changes | Tech Leads, Architects |
| `rules/` | Rarely (after team discussion) | Architects only |
| `skills/` | Quarterly | Tech Leads |
| `templates/` | Annually (new patterns) | Senior Devs |
| `prompts/` | Monthly (refinements) | AI Engineers |
| `tools/` | As needed | DevOps, QA |
| `examples/` | Quarterly | Senior Devs |

---

## ✅ Pre-Deployment Checklist

Before packaging .agents.zip:

- [x] README.md - Framework overview
- [x] QUICK_START.md - Quick start guide
- [x] CODING_STANDARDS.md - Code standards
- [x] ARCHITECTURE_RULES.md - Architecture patterns
- [x] system-prompt.md - Agent system prompt
- [x] ServiceTemplate.java - Service template
- [x] ControllerTemplate.java - Controller template
- [x] docs/ - All business docs migrated
- [x] skills/README.md - Skills registry
- [x] examples/ - Reference implementations

---

## 🚀 How to Use This Framework

### As an AI Agent:
1. Read `QUICK_START.md`
2. Read `CODING_STANDARDS.md`
3. Choose relevant template from `templates/java/`
4. Consult relevant skill from `skills/`
5. Generate code following `prompts/system-prompt.md`
6. Use checklists before submitting

### As a Tech Lead:
1. Review agent outputs against `rules/`
2. Update `docs/` as business changes
3. Add examples to `examples/` for new patterns
4. Refine skills in `skills/` quarterly
5. Update `prompts/` based on agent feedback

### As a Developer:
1. Read relevant business docs in `docs/`
2. Follow templates in `templates/java/`
3. Adhere to rules in `rules/`
4. Study examples in `examples/`
5. Follow commit rules in `rules/git/`

---

## 📝 Notes

- **All MANDATORY rules are marked with 🔴 MANDATORY**
- **TO BE CREATED files are indicated**
- **Existing docs from original .agents/ have been reorganized**
- **No original business documents were deleted**
- **Structure follows agent-skills framework best practices**
- **All files follow Markdown format for consistency**

---

## 📞 Support

If you need clarification on any rule or process:
1. Check `QUICK_START.md` first
2. Read related skill in `skills/`
3. Look for examples in `examples/`
4. Ask your Tech Lead

---

**Framework Version**: 2.0  
**Last Updated**: August 2024  
**Status**: Enterprise Production Ready  


---

## 🔄 Update Log — New Skills Added

**Date**: August 3, 2026

Added missing mandatory/high-value skills per team request:
- `skills/core/CodeReviewSkill.md` — mandatory review checklist + self-review workflow for agents
- `skills/core/CodeAnalysisSkill.md`
- `skills/core/ArchitectureDesignSkill.md`
- `skills/core/SecurityAuditSkill.md`
- `skills/core/TestingSkill.md`
- `skills/microservices/gRPCImplementationSkill.md`
- `skills/microservices/KafkaIntegrationSkill.md`
- `skills/microservices/RedisOptimizationSkill.md`
- `skills/devops/DockerContainerizationSkill.md`
- `rules/git/COMMIT_MESSAGE_RULES.md`
- `rules/git/BRANCH_NAMING_RULES.md`
- `rules/NAMING_CONVENTIONS.md`
- `rules/SECURITY_RULES.md`

No existing business documents (`docs/business/`, `docs/security/`, `docs/observability/`) were modified, moved, or deleted in this update.
