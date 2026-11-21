# Testing

[← Back to README](../README.md)

## Run
```bash
mvn test
```
- **Unit tests** (JUnit 5 + Mockito) need nothing.
- **Integration test** starts a throw-away PostgreSQL 16 container via **Testcontainers** → needs Docker running. If Docker is absent it is skipped automatically.

## What's covered

| Test | Type | Covers |
|---|---|---|
| `PaymentServiceTest` | Unit | partial/full payment, overpayment, already paid, duplicate reference, blank reference, 404s |
| `FeeServiceTest` | Unit | create + normalisation, duplicate fee, bad year, student totals/overdue, summary maths, zero-fee school, unknown school |
| `SchoolServiceTest` | Unit | create + code normalisation, duplicate code (case-insensitive), 404 |
| `StudentServiceTest` | Unit | create + normalisation, unknown school, duplicate admission no. per school, 404 |
| `FeeRecordTest` | Unit | status transitions, entity-level overpayment guard, overdue rule |
| `AcademicYearsTest` | Unit | valid/invalid year formats, century rollover |
| `FeeFlowIntegrationTest` | Integration | full HTTP flow on real PostgreSQL: create → pay → overpay/duplicate → pending list → summary → paid; plus 400/404 responses |

## Manual testing
- Swagger UI: http://localhost:8081/swagger-ui.html (Docker) or http://localhost:8080/swagger-ui.html (local)
- Postman: see [API → Postman](API.md#postman)
