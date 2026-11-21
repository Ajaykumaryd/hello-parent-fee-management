# Local Setup (IDE / Maven)

[← Back to README](../README.md)

Runs the app directly on your machine on port **8080**, using a PostgreSQL on port **5432**.
(Docker uses 8081 / 5433 instead, so both can run together — see [Docker](DOCKER.md).)

## Prerequisites
Java 17+, Maven 3.9+, PostgreSQL 14+ installed locally.

## Run
```bash
sudo -u postgres psql -c "CREATE DATABASE feedb;"   # one time
mvn spring-boot:run                                 # or run FeeManagementApplication in IntelliJ
```
Open http://localhost:8080/swagger-ui.html

Tables are created automatically and demo data is loaded on first start (only if the DB is empty).

## Configuration

| Setting | Default | Env var |
|---|---|---|
| JDBC URL | `jdbc:postgresql://localhost:5432/feedb` | `DB_URL` |
| User / password | `postgres` / `postgres` | `DB_USER` / `DB_PASSWORD` |
| Demo data | on (only if DB is empty) | `SEED_DATA=false` |
| App port | 8080 | `SERVER_PORT` |

Your Postgres password isn't `postgres`? → `DB_PASSWORD=yourpass mvn spring-boot:run`
In IntelliJ: set the same values under *Run Configuration → Environment variables*.

## No local Postgres installed?
Use only the Docker database (port 5433) and run the app locally:
```bash
docker compose up -d postgres
DB_URL=jdbc:postgresql://localhost:5433/feedb mvn spring-boot:run
```

## URLs

| What | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

## Troubleshooting

| Error | Fix |
|---|---|
| `Port 8080 was already in use` | Stop the other process (`fuser -k 8080/tcp`) or `SERVER_PORT=8082 mvn spring-boot:run` |
| `password authentication failed` | Pass your password: `DB_PASSWORD=... mvn spring-boot:run` |
| `database "feedb" does not exist` | Run the `CREATE DATABASE` command above |
| Pressed Ctrl+Z and port still busy | Ctrl+Z only pauses — run `kill %1`; stop with Ctrl+C next time |
