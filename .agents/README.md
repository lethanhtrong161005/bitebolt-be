# BiteBolt Enterprise Agents Framework

Uma estrutura **production-ready** para gerenciar agentes de IA em projetos Java Spring Boot, seguindo padrões de doanh nghiệp 2024.

## 📁 Estrutura do Diretório

```
.agents/
├── README.md                          # Este arquivo (Overview)
├── QUICK_START.md                     # Guia rápido para novos agentes
│
├── docs/                              # Documentação de Negócio & Arquitetura
│   ├── business/                      # Planos de negócio (MUTABLE)
│   │   ├── AUTH_BUSINESS_DESIGN.md
│   │   ├── user-management-plan.md
│   │   └── frontend-architecture/
│   │       └── FRONTEND_ARCHITECTURE_PLAN.md
│   ├── security/                      # Documentação de Segurança
│   │   ├── auth-plans/
│   │   │   ├── PHASE1_INTERNAL_SSO_PLAN.md
│   │   │   ├── PHASE2_EXTERNAL_AUTH_PLAN.md
│   │   │   └── implementation_plan_logout.md
│   │   ├── API_GATEWAY_SECURITY_PLAN.md
│   │   └── USER_CONTEXT_PLAN.md
│   └── observability/                 # Logging & Monitoring
│       └── audit-logging/
│           ├── audit-logging-guide.md
│           └── implementation-plan.md
│
├── skills/                            # Agent Skills (Capacidades Reutilizáveis)
│   ├── README.md                      # Skills Registry & Documentation
│   ├── core/                          # Skills Fundamentais
│   │   ├── CodeAnalysisSkill.md
│   │   ├── ArchitectureDesignSkill.md
│   │   └── SecurityAuditSkill.md
│   ├── microservices/                 # Skills para Microservices
│   │   ├── gRPCImplementationSkill.md
│   │   ├── KafkaIntegrationSkill.md
│   │   └── RedisOptimizationSkill.md
│   ├── frontend/                      # Skills para Frontend
│   │   ├── ReactComponentSkill.md
│   │   └── UIUXConsistencySkill.md
│   └── devops/                        # Skills para DevOps
│       ├── DockerContainerizationSkill.md
│       └── K8sDeploymentSkill.md
│
├── prompts/                           # Sistema de Prompts Estruturado
│   ├── system-prompt.md               # System Prompt Principal
│   ├── code-generation/
│   │   ├── java-service.prompt
│   │   ├── java-controller.prompt
│   │   ├── java-repository.prompt
│   │   ├── java-exception-handler.prompt
│   │   └── java-test.prompt
│   ├── code-review/
│   │   ├── security-review.prompt
│   │   ├── performance-review.prompt
│   │   ├── test-coverage-review.prompt
│   │   └── architecture-review.prompt
│   ├── documentation/
│   │   ├── api-documentation.prompt
│   │   ├── architecture-doc.prompt
│   │   └── deployment-guide.prompt
│   └── analysis/
│       ├── root-cause-analysis.prompt
│       ├── technical-debt.prompt
│       └── migration-planning.prompt
│
├── rules/                             # Regras de Codificação & Padrões
│   ├── CODING_STANDARDS.md            # Padrões de Código (MANDATÓRIO)
│   ├── ARCHITECTURE_RULES.md          # Regras Arquiteturais
│   ├── SECURITY_RULES.md              # Regras de Segurança
│   ├── NAMING_CONVENTIONS.md          # Convenções de Nomenclatura
│   └── git/
│       ├── COMMIT_MESSAGE_RULES.md
│       └── BRANCH_NAMING_RULES.md
│
├── templates/                         # Templates de Código Reutilizáveis
│   ├── java/
│   │   ├── ServiceTemplate.java
│   │   ├── ControllerTemplate.java
│   │   ├── RepositoryTemplate.java
│   │   ├── DTOTemplate.java
│   │   ├── EntityTemplate.java
│   │   ├── ExceptionTemplate.java
│   │   ├── TestTemplate.java
│   │   ├── ConfigTemplate.java
│   │   └── InterceptorTemplate.java
│   ├── xml/
│   │   ├── pom-dependency-template.xml
│   │   └── spring-config-template.xml
│   └── sql/
│       ├── migration-template.sql
│       └── seed-data-template.sql
│
├── tools/                             # Ferramentas & Utilidades
│   ├── code-formatter-config.md       # Configuração de Formatação (Google Style, 2 spaces)
│   ├── checkstyle-config.xml          # CheckStyle Rules
│   ├── spotbugs-config.xml            # Bug Detection
│   └── analysis-scripts/
│       ├── measure-complexity.sh
│       └── validate-coverage.sh
│
├── examples/                          # Exemplos de Implementação
│   ├── service-implementation/
│   │   ├── UserServiceExample.md
│   │   └── UserServiceExample.java
│   ├── api-endpoint/
│   │   ├── UserControllerExample.md
│   │   └── UserControllerExample.java
│   ├── repository/
│   │   ├── UserRepositoryExample.md
│   │   └── UserRepositoryExample.java
│   ├── exception-handling/
│   │   └── GlobalExceptionHandlerExample.java
│   ├── testing/
│   │   ├── ServiceUnitTestExample.java
│   │   └── IntegrationTestExample.java
│   └── microservice-communication/
│       ├── gRPCClientExample.java
│       └── KafkaProducerExample.java
│
└── .gitkeep
```

## 🎯 Propósito de Cada Pasta

| Pasta | Propósito | Quem Modifica |
|-------|----------|---|
| **docs/** | Conhecimento de negócio & arquitetura | Arquitetos, Tech Leads |
| **skills/** | Capacidades de agentes reutilizáveis | Tech Leads, AI Engineers |
| **prompts/** | Instruções estruturadas para agentes | AI Engineers, Senior Devs |
| **rules/** | Padrões obrigatórios (READ-ONLY após aprovação) | Arquitetos (aprovação), Devs (referência) |
| **templates/** | Código boilerplate com padrões corretos | Tech Leads, Senior Devs |
| **tools/** | Linters, formatters, scripts | DevOps, QA |
| **examples/** | Implementações de referência | Senior Devs, AI Trainers |

---

## 🚀 Como Usar Este Framework

### 1️⃣ Iniciando um Novo Agente
```bash
# Leia o Quick Start
cat .agents/QUICK_START.md

# Copie o System Prompt
cp .agents/prompts/system-prompt.md seu-prompt.md

# Use o template apropriado para sua tarefa
cp .agents/templates/java/ServiceTemplate.java MeuService.java
```

### 2️⃣ Coding Standards Essenciais
Todos os agentes **DEVEM** seguir:
- ✅ **Google Java Style** (spaces: 2, tabs: 2)
- ✅ **JavaDoc obrigatório** para classes e métodos
- ✅ **Comentários em INGLÊS** no código
- ✅ **Sem dados sensíveis** em logs
- ✅ **Soft Delete** com `isDeleted` field

📖 Leia: `.agents/rules/CODING_STANDARDS.md`

### 3️⃣ Skills Disponíveis
Antes de começar, veja quais skills seu agente pode usar:
```bash
cat .agents/skills/README.md
```

---

## ✅ Checklist para Agents

Ao gerar código, seu agente DEVE validar:

- [ ] Segue Google Java Style Guide (2 spaces)
- [ ] Todas as classes têm JavaDoc com descrição
- [ ] Todos os métodos públicos têm JavaDoc com @param, @return, @throws
- [ ] Variáveis e métodos estão em camelCase
- [ ] Nenhum comentário com dados sensíveis
- [ ] Nenhum `System.out.println()` - use Logger
- [ ] Nenhum SQL direto - use JPA @Query
- [ ] Exception classes estendem RuntimeException ou HttpException
- [ ] DTOs não contêm lógica de negócio
- [ ] Testes cobrem >80% do código
- [ ] Nomes seguem NAMING_CONVENTIONS.md

---

## 📞 Referência Rápida

| Você quer... | Vá para... |
|---|---|
| Criar um novo Service | `templates/java/ServiceTemplate.java` + `skills/core/CodeAnalysisSkill.md` |
| Criar um novo Controller | `templates/java/ControllerTemplate.java` + `prompts/code-generation/java-controller.prompt` |
| Revisar código | `prompts/code-review/*` + `rules/ARCHITECTURE_RULES.md` |
| Implementar autenticação | `docs/business/AUTH_BUSINESS_DESIGN.md` |
| Configurar logging | `docs/observability/audit-logging/audit-logging-guide.md` |
| Implementar Phân trang & Search | `docs/common-pagination-and-search-guide.md` |
| Integrar gRPC | `skills/microservices/gRPCImplementationSkill.md` |
| Escrever testes | `templates/java/TestTemplate.java` |

---

## 📝 Notas Importantes

1. **Docs são Imutáveis**: Alterações em `docs/` requerem aprovação de Tech Lead
2. **Skills são Instâncias de Conhecimento**: Agents consultam skills como referência
3. **Prompts são Dinâmicos**: Podem ser ajustados sem impacto no código
4. **Templates são Boilerplate Correto**: Sempre começar a partir deles
5. **Rules são Mandatórias**: Violar rules resulta em code review failure

---

