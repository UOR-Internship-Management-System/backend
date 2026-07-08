# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy Maven wrapper and POM first for dependency caching
COPY mvnw pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw -B dependency:resolve dependency:resolve-plugins -DskipTests

# Copy source and build
COPY src src
RUN ./mvnw -B package -DskipTests && \
    mv target/*.jar target/app.jar

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

WORKDIR /app

COPY --from=build /workspace/target/app.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
