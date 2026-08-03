# 🎯 Agent Skills Registry

This directory contains documented **skills** (capabilities) that agents can leverage when working on BiteBolt tasks.

Skills are **references** and **best practices** for specific technical domains. They guide agents on how to implement features correctly.

---

## 📁 Skills Structure

```
skills/
├── core/                    # Fundamental skills
├── microservices/          # Inter-service communication
├── frontend/               # Frontend integration patterns
└── devops/                 # Deployment and operations
```

---

## 🔥 Core Skills

| Skill | File | Use When |
|-------|------|----------|
| **Code Analysis** | `core/CodeAnalysisSkill.md` | Analyzing existing code, identifying patterns |
| **Architecture Design** | `core/ArchitectureDesignSkill.md` | Designing new services or subsystems |
| **Security Audit** | `core/SecurityAuditSkill.md` | Reviewing code for security vulnerabilities |
| **Code Review** | `core/CodeReviewSkill.md` | Reviewing a PR, or self-reviewing generated code before submission |
| **Testing** | `core/TestingSkill.md` | Writing or evaluating unit/integration tests |

---

## 🔗 Microservices Skills

| Skill | File | Use When |
|-------|------|----------|
| **gRPC Implementation** | `microservices/gRPCImplementationSkill.md` | Implementing inter-service communication |
| **Kafka Integration** | `microservices/KafkaIntegrationSkill.md` | Implementing async event processing |
| **Redis Optimization** | `microservices/RedisOptimizationSkill.md` | Adding caching or session management |

---

## 🎨 Frontend Skills

| Skill | File | Use When |
|-------|------|----------|
| **React Components** | `frontend/ReactComponentSkill.md` | Creating React components for BiteBolt UI |
| **UI/UX Consistency** | `frontend/UIUXConsistencySkill.md` | Ensuring UI follows design system |

---

## ⚙️ DevOps Skills

| Skill | File | Use When |
|-------|------|----------|
| **Docker Containerization** | `devops/DockerContainerizationSkill.md` | Creating Docker images for services |
| **K8s Deployment** | `devops/K8sDeploymentSkill.md` | Deploying to Kubernetes *(TODO)* |

---

## 🚀 How to Use Skills

### Example: Implementing gRPC Service

1. **Identify the skill**: I need to implement inter-service communication → gRPC
2. **Read the skill**: Open `skills/microservices/gRPCImplementationSkill.md`
3. **Follow the guidelines**: The skill provides step-by-step implementation patterns
4. **Reference the template**: Use `templates/java/ServiceTemplate.java` as starting point
5. **Consult business docs**: Read `docs/business/AUTH_BUSINESS_DESIGN.md` for context
6. **Write the code**: Implement following all standards

---

## ✅ Skill Quality Checklist

Each skill file should contain:
- [ ] Clear title and description
- [ ] When to use this skill
- [ ] Prerequisites/dependencies
- [ ] Step-by-step implementation guide
- [ ] Code examples (if applicable)
- [ ] Best practices and anti-patterns
- [ ] Related docs and templates
- [ ] Troubleshooting section

---

## 📝 Creating New Skills

When creating a new skill:

1. **Naming**: `[DomainName]Skill.md`
2. **Location**: Place in appropriate category folder
3. **Format**: Use Markdown with clear sections
4. **Examples**: Include practical code examples
5. **References**: Link to related docs and templates
6. **Testing**: Include testing/validation instructions

---

## 🔗 Skill Relationships

```
Code Analysis Skill
├── Architecture Design Skill
├── Security Audit Skill
└── Testing Best Practices

gRPC Implementation Skill
├── Architecture Design Skill
├── Security Audit Skill
└── Kafka Integration Skill

Kafka Integration Skill
├── Redis Optimization Skill
└── Error Handling Patterns
```

---

## 📚 Skill Discovery

**Don't know which skill you need?**

**If you want to...**
| Task | Skill |
|------|-------|
| Build a new service | Architecture Design + Code Analysis |
| Connect services | gRPC Implementation |
| Broadcast events | Kafka Integration |
| Cache data | Redis Optimization |
| Check security | Security Audit |
| Review code quality / a PR | Code Review |
| Write or check tests | Testing |
| Deploy service | Docker + K8s |
| Build UI | React Components + UI/UX Consistency |

---

## 🎓 Learning Path for New Agents

**Recommended learning path for new developers:**

1. Read `.agents/QUICK_START.md` (15 min)
2. Read `.agents/rules/CODING_STANDARDS.md` (30 min)
3. Study `templates/java/ServiceTemplate.java` (20 min)
4. Read `core/CodeAnalysisSkill.md` (20 min)
5. Read `core/ArchitectureDesignSkill.md` (20 min)
6. Read `core/SecurityAuditSkill.md` (20 min)
7. Read `core/CodeReviewSkill.md` (15 min) — **required self-review pass before any code is submitted**
8. Read `core/TestingSkill.md` (15 min)
9. Read relevant business docs in `docs/` (30 min)

**Total time**: ~3 hours to be productive

---

## ✅ Skills Now Available (Updated)

All previously-TODO core skills are now complete:
- ✅ `core/CodeAnalysisSkill.md`
- ✅ `core/ArchitectureDesignSkill.md`
- ✅ `core/SecurityAuditSkill.md`
- ✅ `core/CodeReviewSkill.md` (new)
- ✅ `core/TestingSkill.md` (new)
- ✅ `microservices/gRPCImplementationSkill.md`
- ✅ `microservices/KafkaIntegrationSkill.md`
- ✅ `microservices/RedisOptimizationSkill.md`
- ✅ `devops/DockerContainerizationSkill.md` (new)

Still TODO (lower priority, add as needed): `frontend/*`, `devops/K8sDeploymentSkill.md`.

---

**Last Updated**: August 2024  
**Framework Version**: 2.0

