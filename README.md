# Student Fee Management Module

Spring Boot 3 backend for a school app: create fee records, record payments, view payment history, list students with pending fees and see a school's collection summary.

**Stack:** Java 17 · Spring Boot 3.3 · Spring Data JPA (Hibernate 6) · PostgreSQL · Swagger (springdoc) · JUnit 5 + Mockito + Testcontainers · Docker

## Two ways to run

| | Docker | Local (IDE / Maven) |
|---|---|---|
| Command | `docker compose up -d --build` | `mvn spring-boot:run` |
| Needs | Docker only | Java 17, Maven, PostgreSQL |
| App / Swagger | **http://localhost:8081**/swagger-ui.html | **http://localhost:8080**/swagger-ui.html |
| PostgreSQL | localhost:**5433** | localhost:**5432** |
| Guide | [Docker](docs/DOCKER.md) | [Local setup](docs/LOCAL_SETUP.md) |

Different ports on purpose → both can run at the same time without clashing. Demo data is preloaded in both.

## Documentation

| Doc | What's inside |
|---|---|
| [Overview](docs/OVERVIEW.md) | Architecture diagram, request flow, project structure |
| [Docker](docs/DOCKER.md) | Run app + PostgreSQL in Docker, commands, troubleshooting |
| [Local setup](docs/LOCAL_SETUP.md) | Run from IDE / Maven, configuration, env vars |
| [Database](docs/DATABASE.md) | ER diagram, tables, constraints, indexes |
| [API](docs/API.md) | Endpoints, examples, error format, edge cases, Postman |
| [Design decisions](docs/DESIGN_DECISIONS.md) | Key technical choices and why |
| [Testing](docs/TESTING.md) | Unit + integration tests, how to run |
| [Requirements checklist](docs/REQUIREMENTS_CHECKLIST.md) | Every assignment requirement → where it's done |

**Postman:** `postman/FeeManagement.postman_collection.json`
