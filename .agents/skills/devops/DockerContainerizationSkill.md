# 🐳 Docker Containerization Skill

**Category**: DevOps | **Use When**: Adding or updating a `Dockerfile` for a Spring Boot service.

## 1. Step-by-Step
1. Use a multi-stage build: build stage with the JDK + Maven, runtime stage with a slim JRE only.
2. Pin the base image version explicitly (never `:latest`).
3. Copy only the built jar into the runtime stage — do not ship source code or build tools in the final image.
4. Run the application as a non-root user.
5. Externalize configuration via environment variables / mounted config, never bake secrets into the image.
6. Add a `HEALTHCHECK` (or rely on Kubernetes readiness/liveness probes hitting the Spring Actuator health endpoint).

## 2. Checklist
- [ ] Multi-stage build used.
- [ ] Base image version pinned.
- [ ] Non-root user configured.
- [ ] No secrets baked into image layers.
- [ ] Health endpoint exposed for probes.

## 3. Related References
- `rules/ARCHITECTURE_RULES.md`
