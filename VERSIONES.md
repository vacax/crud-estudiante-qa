# Versiones de dependencias, herramientas e imágenes

Registro de lo que usa el proyecto, con la fecha de publicación de cada versión.

- **Fecha de corte:** 2026-10-27.
- **Regla:** solo se usan versiones con **al menos 14 días de publicadas**, es decir, publicadas el
  **2026-10-13 o antes**. Cuando existe una versión más nueva que no cumple la regla, se anota como
  "descartada".
- **Dónde se declaran:** `gradle/libs.versions.toml` (catálogo), `gradle/wrapper/gradle-wrapper.properties`,
  `Dockerfile`, `compose.yaml` y `.github/workflows/ci.yml`.

## Método de verificación

| Qué | Cómo se verificó |
|---|---|
| Artefactos Maven (Javalin, Jackson, JUnit, Testcontainers, etc.) | `maven-metadata.xml` para ver la versión más reciente y cabecera `Last-Modified` del `.pom` en `repo1.maven.org` (`curl -sI`) |
| Plugins de Gradle (Spotless, PIT) | Cabecera `Last-Modified` del `.pom` del marcador del plugin en `plugins.gradle.org/m2` |
| Gradle | `https://services.gradle.org/versions/all` (campo `buildTime`) |
| GitHub Actions | API de GitHub: `releases` (fecha de publicación) y `git/ref/tags/<tag>` (SHA del commit; las etiquetas anotadas se resuelven al commit con `git/tags/<sha>`) |
| Imágenes Docker | `docker buildx imagetools inspect <imagen:tag>` (digest del índice multi-arquitectura) |

La fecha de un artefacto en Maven Central es la de su carga al repositorio (`Last-Modified`), que
coincide con la de publicación salvo diferencias de horas.

## Build y runtime

| Componente | Versión | Publicada | Notas |
|---|---|---|---|
| Gradle (wrapper) | 9.8.1 | 2026-10-07 | |
| JDK (toolchain) | 25 (LTS) | n/a | Temurin 25.0.3 en local |
| Javalin | 7.2.3 | 2026-08-11 | Descartada: **7.2.4** (2026-10-25), tiene 3 días |
| Jackson `databind`, `jsr310`, `jdk8` (`com.fasterxml`) | 2.22.3 | 2026-09-21 | Es el Jackson que `JavalinJackson` usa por defecto |
| slf4j-simple | 2.0.20 | 2026-09-22 | |
| PostgreSQL JDBC | 42.7.14 | 2026-10-07 | |
| HikariCP | 7.1.0 | 2026-06-14 | |
| Flyway (`flyway-core` + `flyway-database-postgresql`) | 13.10.0 | 2026-10-08 | |

## Pruebas

| Componente | Versión | Publicada | Notas |
|---|---|---|---|
| JUnit (BOM; Jupiter, Platform Launcher y Suite) | 6.1.3 | 2026-08-07 | JUnit 6 es la mayor vigente |
| AssertJ | 3.27.7 | 2026-01-24 | |
| Mockito (`mockito-core`, `mockito-junit-jupiter`) | 5.24.0 | 2026-09-23 | |
| REST Assured | 6.1.0 | 2026-10-09 | |
| Cucumber (BOM) | 8.0.4 | 2026-10-02 | |
| Testcontainers (`testcontainers-postgresql`, `testcontainers-junit-jupiter`) | 2.0.5 | 2026-04-20 | Serie 2.x, sin JUnit 4 |
| Playwright for Java | 1.63.0 | 2026-09-14 | |

## Calidad

| Componente | Versión | Publicada | Notas |
|---|---|---|---|
| Spotless (plugin `com.diffplug.spotless`) | 8.10.4 | 2026-10-08 | |
| google-java-format (vía Spotless) | 1.36.1 | 2026-07-30 | Descartada: **1.37.0** (2026-10-01), incompatible con Spotless 8.10.4 (ver "Desvíos") |
| JaCoCo (`toolVersion`) | 0.8.15 | 2026-06-04 | Descartada: **0.8.16** (2026-10-26), tiene 1 día |
| PIT, plugin de Gradle (`info.solidsoft.pitest`) | 1.19.0 | 2026-03-29 | <!-- revisar al cerrar fase 6 --> |
| PIT (`pitestVersion`) | 1.30.0 | 2026-08-27 | Descartada: **1.30.1** (2026-10-14), tenía 13 días al corte <!-- revisar al cerrar fase 6 --> |
| `pitest-junit5-plugin` | 1.2.3 | 2025-05-20 | <!-- revisar al cerrar fase 6 --> |

## Imágenes Docker

Se fijan por **digest** (además del tag) en `Dockerfile` y `compose.yaml`. Los digests son los que
tenía cada tag el **2026-10-27**.

| Imagen | Uso | Digest (2026-10-27) |
|---|---|---|
| `eclipse-temurin:25-jdk-noble` | Etapa de compilación del `Dockerfile` | `sha256:a9c5ddd386f629518bd500ecfabf7d1e5d32d7ffed735d5c3b1604ae92c830f4` |
| `eclipse-temurin:25-jre-alpine` | Etapa de ejecución del `Dockerfile` | `sha256:b7528962e65d43064d8a8b67ba175367896bc948363ea2d3fcd7f10d34e1f048` |
| `postgres:18-alpine` | `compose.yaml`, Testcontainers (integración y aceptación), documentación | `sha256:10a5c69a83035ab5239f5620d708520e8b28f5aa1f2b20143e6024532edadfb5` |

La misma imagen de PostgreSQL (mismo digest) se usa en `compose.yaml`, en `BasePostgresIT`, en
`Entorno` de aceptación y en `docs/TESTCONTAINERS.md`.

VERIFICAR: la regla de los 14 días **no se aplica igual** a estas imágenes. Un tag como `18-alpine`
es flotante (se mueve con cada parche) y el registro no ofrece una fecha de "publicación" comparable a
la de una versión de Maven. El digest garantiza reproducibilidad desde hoy, pero no demuestra que la
imagen tenga 14 días. No se verificó la fecha de creación de cada digest.

## GitHub Actions

Fijadas por **SHA completo del commit**, con la versión en un comentario.

| Acción | Versión | SHA | Publicada |
|---|---|---|---|
| `actions/checkout` | v7.0.1 | `3d3c42e5aac5ba805825da76410c181273ba90b1` | 2026-07-20 |
| `actions/setup-java` | v6.0.1 | `de7274f081f381c8f8158605e0321c36c376e2e6` | 2026-09-09 |
| `gradle/actions/setup-gradle` | v6.4.0 | `3f5f9adaf7d9fecd50b5935e54106014257a94e6` | 2026-09-28 |
| `actions/upload-artifact` | v7.0.2 | `cf430e030ddbb5b0abf93d22962f4752f3646cd9` | 2026-10-07 |
| `actions/download-artifact` | v8.0.2 | `9000827ccba6bdab643e8b6fd33ac0654aef8333` | 2026-10-07 |
| `EnricoMi/publish-unit-test-result-action` | v2.25.0 | `7622c5bf3d112a72ab4fa3951c2aa5723d8ab92d` | 2026-10-12 |

Descartada: `actions/setup-java` **v6.1.0** (2026-10-14), tenía 13 días al corte. El SHA de
`gradle/actions` corresponde al commit al que apunta la etiqueta anotada `v6.4.0`.

## Desvíos y decisiones

- **google-java-format 1.36.1 en lugar de 1.37.0.** Con Spotless 8.10.4, la 1.37.0 falla con
  `NoSuchMethodError` en `JavaFormatterOptions$Style.valueOf`: Spotless llama a ese método y la 1.37
  lo eliminó. Se reprodujo con un arnés aparte (spotless-lib 4.10.4 + google-java-format 1.37.0). Las
  versiones 1.35.0, 1.36.0 y 1.36.1 funcionan en JDK 25 sin `--add-exports`. Reintentar la 1.37.0
  cuando salga un Spotless que la soporte.
- **JUnit 6.** Es la versión mayor vigente. El prompt habla de JUnit 5, pero la API de Jupiter
  (`@Test`, `@BeforeEach`, `Assertions`, etc.) es la misma; cambia el número de versión y el
  requisito mínimo de Java.
- **Testcontainers 2.x.** Los artefactos llevan el prefijo `testcontainers-`
  (`testcontainers-postgresql`, `testcontainers-junit-jupiter`), la clase del contenedor está en
  `org.testcontainers.postgresql.PostgreSQLContainer` (ya no es genérica con `<SELF>`) y se eliminó el
  soporte de JUnit 4.
- **Javalin 7.** La configuración se hace dentro de `Javalin.create(config -> ...)` y las rutas se
  registran con `config.routes`. La serialización JSON usa `JavalinJackson`, que por defecto emplea
  Jackson 2.x (`com.fasterxml`). Existe `JavalinJackson3` para Jackson 3 (`tools.jackson`), que
  **no** se usa.
- **Cucumber 8 y Jackson.** Para escribir los reportes JSON/HTML Cucumber 8 necesita
  `jackson-datatype-jdk8` además de `databind`; por eso se declara `jackson-jdk8` en la suite de
  aceptación.
- **Gradle 9.8.1: sin `testType`.** `JvmTestSuite` ya no expone `testType`, así que no se declara.
- **Java 25 en CI en lugar de JDK 21.** El prompt menciona JDK 21 en el workflow, pero el stack
  del propio prompt es Java 25 (LTS) y el `Dockerfile` usa 25. Se usa **25 en todos lados** para no
  probar con un JDK distinto del que se despliega. VERIFICAR: confirmar con el autor que 21 era un
  descuido del prompt.
- **Imágenes Docker.** Ver la nota `VERIFICAR` de la sección de imágenes sobre la regla de 14 días.
