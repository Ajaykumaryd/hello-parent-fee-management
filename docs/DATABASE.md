# Database

[← Back to README](../README.md)

PostgreSQL. Schema is generated from the JPA entities.

## ER diagram

```mermaid
erDiagram
    SCHOOLS ||--o{ STUDENTS : has
    STUDENTS ||--o{ FEE_RECORDS : owes
    SCHOOLS ||--o{ FEE_RECORDS : "denormalised for fast school queries"
    FEE_RECORDS ||--o{ PAYMENTS : receives

    SCHOOLS { bigint id PK
              varchar name
              varchar code UK }
    STUDENTS { bigint id PK
               bigint school_id FK
               varchar name
               varchar admission_number "UK with school_id"
               varchar grade }
    FEE_RECORDS { bigint id PK
                  bigint student_id FK
                  bigint school_id FK
                  varchar academic_year "UK(student, year, type)"
                  varchar fee_type
                  numeric total_amount
                  numeric paid_amount
                  date due_date
                  varchar status "PENDING|PARTIAL|PAID"
                  bigint version "optimistic lock" }
    PAYMENTS { bigint id PK
               bigint fee_record_id FK
               numeric amount
               date payment_date
               varchar mode "CASH|UPI|CARD|..."
               varchar transaction_reference UK }
```

All tables also have `created_at`, `updated_at` (`timestamp with time zone`, stored in UTC).

## Tables

| Table | Purpose | Key rules |
|---|---|---|
| `schools` | School (tenant) | `code` unique |
| `students` | Student of a school | `admission_number` unique **per school** |
| `fee_records` | One fee a student owes | unique `(student_id, academic_year, fee_type)`; keeps running `paid_amount` + `status` |
| `payments` | Payment against a fee | `transaction_reference` unique (blocks double entry) |

## Indexes

| Index | Speeds up |
|---|---|
| `fee_records (school_id, status)` | pending list & school summary |
| `fee_records (student_id)` | student fee details |
| `payments (fee_record_id)` | payment history |
| `students (school_id)` | students by school |

## Types
Money = `numeric(12,2)` (Java `BigDecimal`), ids = `bigint` identity, dates = `date`.
