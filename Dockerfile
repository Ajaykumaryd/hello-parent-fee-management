# ---------- Stage 1: build the jar ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom first -> dependency layer is cached until pom.xml changes (fast rebuilds)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
# Tests run separately (`mvn test`); skip here to keep image builds fast
RUN mvn -q -B package -DskipTests

# ---------- Stage 2: small runtime image (JRE only, no Maven / source) ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as non-root for security
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/fee-management-*.jar app.jar
USER app

EXPOSE 8080

# Container is "healthy" once the API answers
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD wget -qO- http://localhost:8080/v3/api-docs > /dev/null || exit 1

# JAVA_OPTS lets you tune memory etc. without rebuilding, e.g. -Xmx512m
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
