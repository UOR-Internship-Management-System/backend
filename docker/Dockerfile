# ---- Build Stage ----
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw -B dependency:resolve dependency:resolve-plugins -DskipTests

COPY src src
RUN ./mvnw -B package -DskipTests && \
    mv target/*.jar target/app.jar

# ---- Runtime Stage ----
# Ubuntu/Jammy is used here because the production CV generator requires a supported XeLaTeX runtime.
FROM eclipse-temurin:21-jre-jammy AS runtime

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        texlive-xetex \
        texlive-latex-base \
        fonts-lmodern \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup --home-dir /app --shell /usr/sbin/nologin appuser

WORKDIR /app
COPY --from=build /workspace/target/app.jar app.jar

RUN mkdir -p /app/data/cv /app/data/academic-ledger \
    && chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
