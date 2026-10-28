# syntax=docker/dockerfile:1

# ---- Etapa 1: compilación (JDK + Gradle wrapper) ----
FROM eclipse-temurin:25-jdk-noble@sha256:a9c5ddd386f629518bd500ecfabf7d1e5d32d7ffed735d5c3b1604ae92c830f4 AS build
WORKDIR /src

# Primero el wrapper y los archivos de build: esta capa se cachea mientras no cambien.
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --version --no-daemon

# Después el código. Las pruebas no se copian (ver .dockerignore).
COPY src ./src
RUN ./gradlew installDist --no-daemon

# ---- Etapa 2: ejecución (JRE mínimo, usuario no root) ----
FROM eclipse-temurin:25-jre-alpine@sha256:b7528962e65d43064d8a8b67ba175367896bc948363ea2d3fcd7f10d34e1f048
RUN addgroup -g 10001 -S app && adduser -u 10001 -S -G app -H -s /sbin/nologin app
WORKDIR /app
COPY --from=build --chown=10001:10001 /src/build/install/crud-estudiante-qa/ ./
USER 10001:10001
EXPOSE 7070
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=5 \
  CMD wget -qO- http://127.0.0.1:7070/health || exit 1
ENTRYPOINT ["/app/bin/crud-estudiante-qa"]
