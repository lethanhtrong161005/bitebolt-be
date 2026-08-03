# 🔗 gRPC Implementation Skill

**Category**: Microservices | **Use When**: Implementing internal service-to-service communication.

## 1. When To Use gRPC vs REST
Per `rules/ARCHITECTURE_RULES.md`: gRPC for **internal** service-to-service calls; REST stays for external/client-facing APIs. Do not expose gRPC directly to browser/mobile clients.

## 2. Step-by-Step
1. Define the contract in a `.proto` file first (request/response messages, service methods) — treat it as the API design step.
2. Generate stubs (`mvn generate-sources` via the protobuf plugin); never hand-edit generated code.
3. Implement the service by extending the generated `*ImplBase`, delegating immediately to the existing Service-layer class (do not duplicate business logic in the gRPC adapter).
4. Map internal exceptions to gRPC `Status` codes explicitly (e.g., `NOT_FOUND`, `INVALID_ARGUMENT`, `PERMISSION_DENIED`) — never let a raw exception leak as `UNKNOWN`.
5. Propagate the correlation/trace id via gRPC metadata (interceptor), matching the MDC pattern used for REST.
6. Configure TLS for the channel in non-local environments.
7. Add a timeout and retry policy (idempotent calls only) on the client stub.

## 3. Checklist
- [ ] `.proto` reviewed before implementation.
- [ ] Adapter delegates to existing Service layer, no duplicated logic.
- [ ] Exceptions mapped to explicit gRPC status codes.
- [ ] Correlation id propagated.
- [ ] Timeout/retry configured on client.
- [ ] TLS enabled outside local dev.

## 4. Related References
- `rules/ARCHITECTURE_RULES.md`
- `skills/core/ArchitectureDesignSkill.md`
