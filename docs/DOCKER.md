# Docker

[← Back to README](../README.md)

Runs the **app + PostgreSQL** together. Only Docker is needed (no Java, Maven or Postgres install).

## Start

```bash
docker compose up -d --build
```

| What | URL / address |
|---|---|
| Swagger UI | http://localhost:8081/swagger-ui.html |
| API base | http://localhost:8081/api/v1 |
| PostgreSQL (from your machine) | `localhost:5433`, db `feedb`, user/pass `postgres` |

> Docker uses ports **8081 / 5433**, not 8080 / 5432, so it never clashes with a [local run](LOCAL_SETUP.md) or a locally installed Postgres. Both can run at the same time.

Demo data (2 schools, 4 students, 5 fees) is loaded on first start.

## Commands

| Task | Command |
|---|---|
| Build & start | `docker compose up -d --build` |
| Watch app logs | `docker compose logs -f app` |
| Status / health | `docker compose ps` |
| Stop | `docker compose down` |
| Stop + delete DB data (fresh demo data next start) | `docker compose down -v` |
| Open psql | `docker exec -it fee-postgres psql -U postgres -d feedb` |

## Containers

| Service | Image | Your machine → container | Notes |
|---|---|---|---|
| `app` (`fee-app`) | built from `Dockerfile` | **8081** → 8080 | starts only after Postgres is healthy |
| `postgres` (`fee-postgres`) | `postgres:16-alpine` | **5433** → 5432 | data kept in volume `fee-pgdata` |

- Inside Docker the app reaches the DB at `postgres:5432` (service name) — set by `DB_URL` in `docker-compose.yml`. You don't need to change it.
- Need other host ports? `APP_HOST_PORT=9090 POSTGRES_HOST_PORT=5434 docker compose up -d`

## Seed data

| Situation | Seeded? |
|---|---|
| First `docker compose up` | ✅ yes |
| Restart / `down` + `up` | ❌ no — DB is kept in the volume, no duplicates |
| After `docker compose down -v` | ✅ yes (DB was wiped) |

Turn off: set `SEED_DATA: "false"` in `docker-compose.yml`.

## Dockerfile (multi-stage)

| Stage | Base image | Does |
|---|---|---|
| build | `maven:3.9-eclipse-temurin-17` | downloads dependencies (cached layer), builds the jar |
| runtime | `eclipse-temurin:17-jre-alpine` | runs only the jar — small image, no source/Maven |

Runs as non-root user · healthcheck on `/v3/api-docs` · JVM options via `JAVA_OPTS`.

## Troubleshooting

| Error | Fix |
|---|---|
| `bind ... 8081` or `5433 ... address already in use` | Another program uses that port → pick others: `APP_HOST_PORT=9090 POSTGRES_HOST_PORT=5434 docker compose up -d` |
| App keeps restarting | `docker compose logs app` – usually DB not reachable |
| Code changes not visible | Rebuild: `docker compose up -d --build` |
| Want fresh demo data | `docker compose down -v` then `docker compose up -d` |
