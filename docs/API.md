# API

[← Back to README](../README.md)

Base path: `/api/v1` · JSON

| Running via | Swagger UI | API base |
|---|---|---|
| Docker | http://localhost:8081/swagger-ui.html | http://localhost:8081/api/v1 |
| Local (IDE / Maven) | http://localhost:8080/swagger-ui.html | http://localhost:8080/api/v1 |

## Endpoints

| # | Feature | Method & Path | Success |
|---|---|---|---|
| 1 | Create fee record | `POST /fees` | 201 + `Location` |
| 2 | Record payment | `POST /fees/{feeId}/payments` | 201 (payment + updated fee) |
| 3 | Fee details of a student | `GET /students/{studentId}/fees?status=` | 200 |
| 4 | Payment history of a fee | `GET /fees/{feeId}/payments?page=&size=` | 200 (paged, newest first) |
| 5 | Students with pending fees | `GET /fees/pending-students?schoolId=&academicYear=&overdueOnly=&page=&size=` | 200 (paged, highest outstanding first) |
| 6 | School fee summary | `GET /schools/{schoolId}/fee-summary?academicYear=` | 200 |
| – | Setup / lookup | `POST /schools`, `GET /schools/{id}`, `POST /students`, `GET /students/{id}`, `GET /fees/{id}` | 201 / 200 |

Pagination: `page` starts at 0, `size` 1–100 (default 20). Response:
```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3, "last": false }
```

## Examples

**Create fee** `POST /api/v1/fees`
```json
{ "studentId": 1, "academicYear": "2025-26", "feeType": "TUITION",
  "description": "Annual tuition", "totalAmount": 50000.00, "dueDate": "2026-04-30" }
```

**Record payment** `POST /api/v1/fees/1/payments`
```json
{ "amount": 20000.00, "mode": "UPI", "paymentDate": "2026-01-15",
  "transactionReference": "UPI-889911", "remarks": "Installment 1" }
```
`mode`: `CASH | UPI | CARD | NET_BANKING | CHEQUE | BANK_TRANSFER`. `paymentDate` defaults to today.

## Status codes

| Code | When |
|---|---|
| 200 / 201 | Success / created |
| 400 | Validation failed, bad JSON, bad parameter |
| 404 | School / student / fee not found |
| 409 | Duplicate data, fee already paid, concurrent update |
| 422 | Business rule broken (overpayment) |
| 500 | Unexpected error (logged, generic message) |

## Error format (all errors)
```json
{ "timestamp": "…", "status": 422, "error": "Unprocessable Entity",
  "code": "PAYMENT_EXCEEDS_OUTSTANDING",
  "message": "Payment 700.00 exceeds outstanding amount 600.00",
  "path": "/api/v1/fees/1/payments",
  "fieldErrors": { "field": "only present on validation errors" } }
```

## Edge cases handled

| Case | Result |
|---|---|
| Missing/invalid fields, amount ≤ 0, >2 decimals, future payment date | 400 + `fieldErrors` |
| Bad JSON / unknown enum / bad date / `id=abc` / `size>100` | 400 |
| Academic year not `YYYY-YY` or not consecutive (`2025-27`) | 400 |
| Unknown school / student / fee (incl. filters, history) | 404 (never a silent empty list) |
| Duplicate school code, admission no. (per school), fee (student+year+type) | 409 |
| Duplicate `transactionReference` (client retry / double click) | 409, nothing recorded |
| Payment on fully paid fee | 409 `FEE_ALREADY_PAID` |
| Overpayment | 422 with outstanding amount in message |
| Two payments on the same fee at the same moment | serialised by row lock → no overshoot |
| Race past service pre-checks | DB unique keys → 409 |
| School with no fees in summary | zeros, no divide-by-zero |
| Case/space variants (`" tuition fee "`, `dps-ggn`) | normalised (`TUITION_FEE`, `DPS-GGN`) |

## Postman
Import `postman/FeeManagement.postman_collection.json`. Run folders in order (Setup → Fees → Payments → Summary → Error cases); created ids are saved automatically. Set collection variable `baseUrl` to match how you run the app:

| Running via | `baseUrl` |
|---|---|
| Docker (default in collection) | `http://localhost:8081` |
| Local (IDE / Maven) | `http://localhost:8080` |

(Collection → *Variables* tab → edit `baseUrl` → Save.)
