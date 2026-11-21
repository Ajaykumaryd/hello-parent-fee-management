# Overview

[← Back to README](../README.md)

## Architecture

```mermaid
flowchart LR
    C[Client / Postman / Swagger] --> CT[Controllers<br/>REST + validation]
    CT --> S[Services<br/>business rules + @Transactional]
    S --> R[Repositories<br/>Spring Data JPA]
    R --> DB[(PostgreSQL)]
    CT -. errors .-> EH[GlobalExceptionHandler<br/>one JSON error shape]
```

| Layer | Responsibility |
|---|---|
| Controller | HTTP mapping, input validation, status codes. No business logic |
| Service | Business rules, transactions, entity → DTO mapping |
| Repository | DB access, aggregate queries (pending list, summary) |
| Exception handler | Turns every error into the same JSON shape |

## Request flow – recording a payment

```mermaid
sequenceDiagram
    participant C as Client
    participant PC as PaymentController
    participant PS as PaymentService
    participant DB as PostgreSQL
    C->>PC: POST /fees/{id}/payments
    PC->>PC: validate body (@Valid)
    PC->>PS: recordPayment()
    PS->>DB: duplicate reference? (409)
    PS->>DB: SELECT fee FOR UPDATE (row lock)
    PS->>PS: already paid? (409) / overpay? (422)
    PS->>DB: insert payment + update fee balance/status
    PS-->>C: 201 payment + updated fee
```

## Project structure

```
src/main/java/com/helloparent/fees
├── controller   REST endpoints only (no logic)
├── service      business rules, transactions
├── repository   JPA repos + aggregate JPQL (projection/)
├── entity       School, Student, FeeRecord, Payment
├── dto          request/ response/ records
├── exception    typed exceptions + GlobalExceptionHandler
└── config       Clock, Swagger info, demo DataSeeder
src/main/resources/application.yml   PostgreSQL config
src/test/resources/application.yml   test overrides (DB from Testcontainers)
docs/                                this documentation
postman/                             Postman collection
Dockerfile                           app image (multi-stage)
docker-compose.yml                   app + PostgreSQL 16
```
