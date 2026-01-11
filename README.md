# Production-Ready API -- Chapter Lead Assignment

## Overview

This project is a **production-ready REST API** implemented in **Java
with Spring Boot**, designed to demonstrate **Chapter-lead
level engineering practices** rather than just functional correctness.

The focus is on: 
- Clear architectural boundaries 
- Operational readiness 
- Test strategy and quality gates 
- Explicit design decisions and trade-offs

The business use case is intentionally simple to keep attention on
**engineering quality**.

------------------------------------------------------------------------

## Architecture

### Chosen Architecture: Lightweight Hexagonal (Ports & Adapters)

A **lightweight hexagonal architecture** was selected because the given
business case is simple and does not justify full domain decomposition
overhead.

**Packages:** 
- `application` -- Use cases and business orchestration 
- `port` -- Input and output contracts (interfaces) 
- `adapter` -- Infrastructure concerns (web, persistence)

**Key Principles Applied:** 
- Business logic is isolated from frameworks 
- Controllers contain no domain logic 
- Infrastructure depends on application layer, not vice versa 
- Ports define behavior, adapters implement it

This structure allows the application to evolve toward a fuller
hexagonal or clean architecture if complexity increases.

Please Check the ADR(Architectural Decision Record) [here](ADR.md)

------------------------------------------------------------------------

## API Design

-   RESTful endpoints
-   OpenAPI (Swagger) contract-first approach
-   Validation handled via Bean Validation (`@Valid`)
-   Consistent HTTP status codes

### API Documentation

The API contract is defined using an **OpenAPI YAML file** and can be
visualized via Swagger UI.

------------------------------------------------------------------------

## Error Handling Strategy

Errors are handled centrally using a global exception handler.

### Validation Errors

-   Input validation errors are caught early
-   Clear, client-friendly error responses are returned

### Application Errors

-   Application-level exceptions are explicitly modeled
-   Controllers never leak internal exceptions

This ensures predictable API behavior and easier client integration.

------------------------------------------------------------------------

## Observability & Operations

### Metrics

-   Spring Boot Actuator enabled
-   Prometheus-compatible metrics exposed
-   Key metrics available:
    -   HTTP request count
    -   HTTP request latency
    -   Error rates

### Health Probes

-   Liveness probe
-   Readiness probe

### Logging & Correlation

- **Structured Logs**: Each HTTP request generates a single structured log line at completion.
- **Correlation ID**: Every request is assigned an `X-Correlation-Id` which is propagated in logs and returned in the response headers. This allows tracing individual requests across logs.
- **Log Levels by Status Code**:
    - `INFO` for 2xx success responses
    - `WARN` for 4xx client errors
    - `ERROR` for 5xx server errors
- **Why this matters**:
    - Metrics detect trends and anomalies (“what is slow?”)
    - Logs with correlation IDs allow root-cause investigation (“which request was slow or failed?”)
    - One log per request balances observability with performance and avoids log noise

These allow the service to be safely deployed in containerized
environments.

------------------------------------------------------------------------

## Testing Strategy

Testing follows a **test pyramid** approach.

### Unit Tests

-   Focused on application logic
-   External dependencies mocked

### Integration Tests

-   Verifying adapter-to-application wiring
-   Persistence and API layer validation

### Mutation Testing

-   Ensures tests assert behavior, not implementation
-   Guards against false-positive test coverage

This combination provides confidence in both correctness and
maintainability.
------------------------------------------------------------------------

## Code Quality & Design Principles

The codebase intentionally demonstrates: 
- SOLID principles 
- Immutability where possible 
- Meaningful naming 
- Clear package boundaries 
- No business logic in controllers

These practices reflect standards expected to be applied across a team.

------------------------------------------------------------------------

## Containerization

-   Dockerfile included
-   Application can be built and run as a container

This supports reproducible builds and deployment consistency.

------------------------------------------------------------------------

## What Was Intentionally Not Implemented

### Resilience Patterns

-   Circuit breakers
-   Bulkheads
-   Timeouts on external calls

These were intentionally omitted because the application does **not
currently depend on external services**.

However, the architecture supports introducing these patterns at adapter
boundaries if external integrations are added.

------------------------------------------------------------------------

## Future Improvements

Given more time or a more complex domain, the following would be added:

-   API versioning strategy
-   Domain-level error taxonomy
-   Rate limiting
-   Security (JWT / OAuth2)
-   ADRs (Architecture Decision Records)
-   Contract testing for adapters
-   Distributed tracing
-   CI/CD

------------------------------------------------------------------------

## How to Run

``` bash
./mvnw clean package
java -jar target/MortgageCheckAPI-*.jar
```

Or using Docker:

``` bash
docker build -t production-api .
docker run -p 8080:8080 production-api
```

