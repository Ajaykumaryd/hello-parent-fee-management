# Design Decisions

[← Back to README](../README.md)

| Decision | Why |
|---|---|
| `paidAmount` + `status` stored on the fee, updated in the same transaction as the payment insert | Lists and summaries never re-sum the payments table |
| `SELECT … FOR UPDATE` when paying + `@Version` | Two parallel payments can't overpay a fee |
| PostgreSQL | Strong transactions, row locks, exact `numeric` money type |
| `BigDecimal` / `numeric(12,2)` for money | No floating-point rounding errors |
| Aggregation in the DB (`GROUP BY`, `SUM(CASE…)`) | Pending list and summary = one query each, constant memory |
| `school_id` denormalised on `fee_records` + index `(school_id, status)` | Fast school-level reports without extra joins |
| Status derived (`PENDING → PARTIAL → PAID`), `overdue` computed at read time | Clients can't set inconsistent states |
| Unique `transactionReference` | Safe client retries — no double payment |
| Injected `Clock` (Asia/Kolkata) | "Today/overdue" logic is testable and timezone-correct |
| Java records as DTOs, entities never exposed | Clean API contract; DB changes don't leak |
| Custom `PageResponse` | Small, stable pagination JSON |
| Typed exceptions + one `GlobalExceptionHandler` | Consistent errors with machine-readable `code` |
| `open-in-view=false`, `readOnly` transactions on reads | No hidden lazy-loading queries |
| Page size capped at 100 | Protects the DB from huge requests |
| Testcontainers for integration test | Tests run on the same DB as production |
| Multi-stage Docker image, non-root | Small, secure runtime image |

## Production notes
- Replace `ddl-auto=update` with Flyway/Liquibase migrations.
- Add authentication (e.g. Spring Security + JWT) and restrict data per school.
