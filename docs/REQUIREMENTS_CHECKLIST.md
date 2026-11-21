# Requirements Checklist

[← Back to README](../README.md)

Every requirement from the assignment and where it is implemented.

## Technology
| Requirement | Done | Where |
|---|---|---|
| Java 17 or 21 | ✅ | `pom.xml` (Java 17) |
| Spring Boot 3.x | ✅ | Boot 3.3.5 |
| Maven or Gradle | ✅ | Maven |
| Database | ✅ | PostgreSQL |
| Spring Data JPA / Hibernate | ✅ | `repository/`, `entity/` |
| REST APIs | ✅ | `controller/` |

## Functionality
| Requirement | Done | Endpoint |
|---|---|---|
| Create a fee record for a student | ✅ | `POST /api/v1/fees` |
| Record a payment against a student's fee | ✅ | `POST /api/v1/fees/{feeId}/payments` |
| Retrieve fee details for a student | ✅ | `GET /api/v1/students/{studentId}/fees` |
| Retrieve payment history for a fee record | ✅ | `GET /api/v1/fees/{feeId}/payments` |
| List students with pending fees + school filter + pagination | ✅ | `GET /api/v1/fees/pending-students?schoolId=&page=&size=` |
| Summary of collection & outstanding for a school | ✅ | `GET /api/v1/schools/{schoolId}/fee-summary` |

## API expectations
| Requirement | Done | Where |
|---|---|---|
| Suitable HTTP methods, params, formats, status codes | ✅ | [API](API.md) |
| Pagination wherever relevant | ✅ | payment history, pending students |

## Development expectations
| Requirement | Done | Where |
|---|---|---|
| Clear, maintainable structure | ✅ | [Overview](OVERVIEW.md) |
| Controller / service / data-access separated | ✅ | `controller/`, `service/`, `repository/` |
| Request & response DTOs | ✅ | `dto/request`, `dto/response` |
| Centralized exception handling | ✅ | `GlobalExceptionHandler` |
| Transaction management | ✅ | `@Transactional` services, row lock on payment |
| Unit tests for service layer | ✅ | [Testing](TESTING.md) |
| Readable, modular, easy to extend | ✅ | layered packages, commented code |

## Submission
| Requirement | Done | Where |
|---|---|---|
| Source code in a Git repository | ✅ | GitHub repo |
| README with setup & execution steps | ✅ | [README](../README.md), [Docker](DOCKER.md), [Local setup](LOCAL_SETUP.md) |
| Postman collection / API docs | ✅ | `postman/`, Swagger UI |
| Unit tests | ✅ | `src/test/` |
| Explanation of implementation & decisions | ✅ | [Design decisions](DESIGN_DECISIONS.md) |

## Extras
Swagger UI, Docker + docker compose, Testcontainers integration test, demo data, detailed edge-case handling.
