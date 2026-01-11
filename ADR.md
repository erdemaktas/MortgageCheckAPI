# ADR-001: Lightweight Hexagonal Architecture with In-Memory Adapter

## Status

**Accepted**

------------------------------------------------------------------------

## Context

The assignment explicitly requires that **data must be stored in
memory**, with no database or external cache.

At the same time, the assignment is intended to demonstrate: -
Production-readiness - Architectural decision-making - Extensibility and
maintainability - Tech Lead / Chapter Lead--level judgment

While persistence is intentionally simplified, real-world production
systems typically evolve to use databases or external storage.
Therefore, the solution should **support change without refactoring core
business logic**.

------------------------------------------------------------------------

## Decision

We chose to implement a **lightweight hexagonal architecture (Ports &
Adapters)** and model the in-memory storage as an **infrastructure
adapter**.

The application is structured into the following core packages:

-   **application**\
    Contains use cases and business orchestration logic.

-   **port**\
    Defines input and output contracts (interfaces), including
    persistence ports.

-   **adapter**\
    Contains infrastructure implementations, including an **in-memory
    persistence adapter**.

The in-memory data store is treated as a **replaceable adapter**, not as
a core part of the application logic.

------------------------------------------------------------------------

## Rationale

This decision was made to satisfy the assignment constraints while
preserving **architectural integrity and future flexibility**.

Key reasons:

1.  **Requirement compliance**\
    The in-memory implementation fully satisfies the given constraints.

2.  **Separation of concerns**\
    Business logic is independent of how data is stored.

3.  **Ease of change**\
    Replacing the in-memory adapter with a database-backed adapter would
    require:

    -   No changes to application logic\
    -   No changes to API contracts\
    -   Only a new adapter implementation

4.  **Avoidance of premature complexity**\
    A full persistence abstraction stack would be unnecessary for the
    current scope.

5.  **Demonstration of real-world thinking**\
    Even when requirements are simple, architectural boundaries are
    preserved.

This approach demonstrates **how a tech lead designs for change without
overengineering**.

------------------------------------------------------------------------

## Consequences

### Positive

-   Clear isolation of infrastructure concerns
-   High testability of application logic
-   Easy replacement of in-memory storage with database or cache
-   Clean adherence to dependency inversion
-   Architecture scales with system complexity

### Negative

-   Slightly more structure than a monolithic in-memory implementation
-   Some abstractions may appear unnecessary for a purely temporary
    solution

These trade-offs were intentionally accepted to favor **long-term
maintainability and clarity**.

------------------------------------------------------------------------

## Alternatives Considered

### Direct In-Memory Storage in Application Layer

**Rejected**

-   Would tightly couple business logic to storage mechanism\
-   Would require refactoring when persistence changes\
-   Sends the wrong signal for production-quality design

------------------------------------------------------------------------

### Layered Architecture with In-Memory Repository

**Rejected**

-   Provides weaker boundaries\
-   Easier for infrastructure concerns to leak into business logic\
-   Less explicit about dependency direction

------------------------------------------------------------------------

### Full Clean Architecture

**Rejected**

-   Considered over-engineering for the assignment scope\
-   Would reduce readability without adding proportional value

------------------------------------------------------------------------

## Future Considerations

If persistence requirements evolve: - Introduce a database-backed
adapter implementing the same port - Add transactional boundaries -
Introduce migration strategy and data consistency guarantees - Add
resilience patterns around persistence adapters if externalized
