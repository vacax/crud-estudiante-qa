# crud-estudiante-qa

[![CI](https://github.com/vacax/crud-estudiante-qa/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/vacax/crud-estudiante-qa/actions/workflows/ci.yml?query=branch%3Amain)

Aplicación **didáctica** para la lámina *"Testing automatizado y QA en DevOps"* del curso
**MCW-621 Ciberseguridad en la Nube** (PUCMM).

El dominio es deliberadamente trivial, un CRUD de estudiantes. Lo importante no es la aplicación,
sino **demostrar cada nivel de prueba** (unitarias, integración, aceptación, end to end, TDD y
Testcontainers) y cómo un pipeline de CI los ejecuta como *gates* de calidad.

## Contenido

- [Arquitectura](#arquitectura)
- [Requisitos y ejecución](#requisitos-y-ejecución)
- [Niveles de prueba](#niveles-de-prueba)
- [Calidad: cobertura, mutation testing y estilo](#calidad-cobertura-mutation-testing-y-estilo)
- [Pipeline de CI](#pipeline-de-ci)
- [Defectos sembrados](#defectos-sembrados)
- [Documentación adicional](#documentación-adicional)

## Arquitectura

```
controlador (Javalin) → servicio (reglas de negocio) → repositorio (JDBC) → PostgreSQL
```

| Capa | Clase | Nivel de prueba natural |
|---|---|---|
| Dominio | `dominio.ValidadorEstudiante`, `Carrera`, `Estudiante` | Unitarias |
| Servicio | `servicio.EstudianteService` (depende de la **interfaz** `EstudianteRepositorio`) | Unitarias con Mockito |
| Repositorio | `repositorio.EstudianteRepositorioJdbc` + migraciones Flyway | Integración (PostgreSQL real) |
| Web | `web.EstudianteControlador`, `Aplicacion` | Aceptación (HTTP real) |
| Interfaz + despliegue | `public/index.html`, `Dockerfile`, `compose.yaml` | End to end (navegador real) |

**Entidad `Estudiante`:** `id` generado por la BD; `nombre` obligatorio, de 2 a 100 caracteres y sin
espacios al inicio ni al final; `carrera` obligatoria y del catálogo; `correo` obligatorio y con
formato válido. Además, por la regla implementada con TDD, no puede haber dos estudiantes con el
mismo nombre en la misma carrera.

> **El catálogo de carreras (`ISC`, `ITT`, `IME`, `IIS`, `ICI`) es didáctico.** No representa la
> oferta académica real de ninguna institución.

### API REST

| Método | Ruta | Respuesta |
|---|---|---|
| `GET` | `/api/estudiantes` | 200, lista |
| `GET` | `/api/estudiantes/{id}` | 200 o 404 |
| `POST` | `/api/estudiantes` | 201 con `Location`, 400 (validación) o 409 (duplicado) |
| `PUT` | `/api/estudiantes/{id}` | 200, 400, 404 o 409 |
| `DELETE` | `/api/estudiantes/{id}` | 204 o 404 |
| `GET` | `/health` | 200 si la BD responde, 503 si no |

Errores con formato uniforme:

```json
{"errores": [{"campo": "nombre", "mensaje": "El nombre debe tener entre 2 y 100 caracteres"}]}
```

La página `http://localhost:7070/` (HTML + `fetch`, sin frameworks) existe para que la prueba E2E
tenga un navegador que manejar. Cada elemento interactivo tiene su `data-testid`.

## Requisitos y ejecución

- JDK 25 (el wrapper de Gradle 9.8.1 está incluido: no hace falta instalar Gradle).
- Docker con `docker compose` (para integración, aceptación, E2E y para levantar el stack).

Levantar el stack completo:

```bash
cp .env.ejemplo .env          # ajuste las credenciales; .env no se versiona
docker compose up --build -d
curl http://localhost:7070/health
docker compose down -v
```

La base de datos **no publica su puerto** al host: solo la alcanza la aplicación por la red
interna de compose.

## Niveles de prueba

Cada nivel es una suite independiente del plugin **JVM Test Suites** de Gradle, con su propia
carpeta en `src/` y su propia tarea.

| Nivel | Qué valida | Qué **no** valida | Herramientas | Tiempo aprox.* | Comando |
|---|---|---|---|---|---|
| **1. Unit** (`src/test`) | Reglas de validación (límites de longitud, espacios, catálogo, correo) y lógica del servicio: inexistentes, duplicados, que lo inválido no llegue al repositorio | SQL, mapeo de columnas, restricciones de la BD, HTTP, JSON, la UI, el despliegue | JUnit 6 (Jupiter), AssertJ, Mockito, `@ParameterizedTest` | ≈ 10 s | `./gradlew test` |
| **2. Integración** (`src/integrationTest`) | Repositorio JDBC contra PostgreSQL real: mapeo de columnas, generación del `id`, migraciones Flyway desde cero, restricciones `CHECK`/`NOT NULL`/`UNIQUE` (SQLState) | Reglas del servicio, HTTP, la UI, la imagen Docker | JUnit, AssertJ, Testcontainers (PostgreSQL 18), Flyway | ≈ 12 s | `./gradlew integrationTest` |
| **3. Aceptación** (`src/acceptanceTest`) | El comportamiento observable de la API tal como lo pide el usuario: códigos HTTP exactos (201 + `Location`, 400, 404, 409, 204), formato de errores | La UI, la imagen Docker, `compose.yaml`, la configuración por variables de entorno | Cucumber (Gherkin **en español**), REST Assured, Testcontainers; app levantada en el proceso de prueba | ≈ 18 s | `./gradlew acceptanceTest` |
| **4. End to end** (`src/e2eTest`) | El sistema **tal como se despliega**: imagen Docker real + PostgreSQL vía el mismo `compose.yaml`, la página HTML en un navegador real, el `HEALTHCHECK` | Casos límite de validación y reglas finas (eso lo cubren los niveles inferiores, más rápidos y precisos) | Testcontainers `ComposeContainer`, Playwright for Java (Chromium), REST Assured | ≈ 1 min 44 s (incluye construir la imagen, ~28 s) | `./gradlew e2eTest` |
| **TDD** | Evidencia en el historial del ciclo rojo → verde → refactor | — | — | — | ver [`docs/TDD.md`](docs/TDD.md) |

\* Medido en la máquina del autor (22 núcleos, imágenes ya descargadas), ejecutando cada tarea
sola, sin caché de Gradle (`--rerun`) y sin daemon: incluye el arranque de Gradle (~5 s). La primera vez hay que sumar la descarga de imágenes
Docker y de Chromium.

Notas por nivel:

- **Unit:** sin BD, sin red y sin Javalin. Los casos límite (1, 2, 100 y 101 caracteres, espacios,
  carrera en minúsculas o fuera del catálogo, correos inválidos) son `@ParameterizedTest`. Los datos
  salen de `EstudianteBuilder` (*test data builder* en `src/testFixtures`, compartido por todas las
  suites); no hay datos aleatorios.
- **Integración:** un contenedor PostgreSQL **compartido por toda la suite** (singleton), porque
  arrancarlo cuesta segundos y las pruebas no necesitan aislamiento de servidor; el aislamiento se
  logra con `TRUNCATE estudiante RESTART IDENTITY` antes de cada prueba. Ver
  [`docs/TESTCONTAINERS.md`](docs/TESTCONTAINERS.md).
- **Aceptación:** `src/acceptanceTest/resources/features/estudiantes.feature` (`# language: es`), con
  `Característica`, `Antecedentes`, `Escenario`, `Esquema del escenario` y `Ejemplos`. Reportes en
  `build/reports/cucumber/`.
- **E2E:** Playwright localiza **solo** por `data-testid`. Si una prueba falla, guarda la captura
  (`build/reports/e2e/capturas/*.png`) y el trace (`build/reports/e2e/traces/*.zip`, se abre con
  `npx playwright show-trace` o en <https://trace.playwright.dev>). `e2eTest` construye antes la
  imagen (`construirImagen`); con `-PomitirConstruccionImagen` usa una ya cargada (así lo hace CI).
  Playwright descarga Chromium la primera vez; en un Linux limpio:
  `./gradlew instalarNavegadores -PplaywrightArgs="install --with-deps chromium"`.

Todo junto, en local:

```bash
./gradlew spotlessCheck test integrationTest acceptanceTest e2eTest \
          jacocoReporteCombinado verificarCobertura pitest
```

## Calidad: cobertura, mutation testing y estilo

| Gate | Herramienta | Comando | Umbral |
|---|---|---|---|
| Formato | Spotless + google-java-format | `./gradlew spotlessCheck` (`spotlessApply` para corregir) | Cualquier diferencia falla |
| Cobertura combinada | JaCoCo (unit + integración + aceptación) | `./gradlew jacocoReporteCombinado verificarCobertura` | Paquete `servicio`: líneas ≥ 90 %, ramas ≥ 85 % |
| Mutation testing | PIT sobre `servicio` y `dominio` | `./gradlew pitest` | Mutation score ≥ 85 % |

**¿Por qué 90 % / 85 % en el servicio?** Es la capa con las reglas de negocio, así que debe estar
casi totalmente ejercitada. Exigir 100 % vuelve frágil el build ante cualquier línea trivial
nueva, y por debajo de ~80 % quedan reglas sin probar. Hoy mide 100 % de líneas y de ramas. El umbral se
aplica al reporte combinado y **hace fallar el build** (job `reportes` de CI). No está enganchado a
`check` para que `check` no exija Docker.

Cobertura combinada actual: 

| Paquete | Líneas | Ramas |
|---|---|---|
| `servicio` | 100 % | 100 % |
| `dominio` | 94,9 % | 96,7 % |
| `web` | 82,2 % | 50,0 % |
| `repositorio` | 77,5 % | 78,6 % |
| `config` | 68,8 % | 0 % |
| **Total** | **80,3 %** | **78,3 %** |

La E2E no aporta al reporte combinado: la aplicación corre dentro de su contenedor, sin agente JaCoCo.

### Cobertura ≠ calidad de las pruebas

La cobertura dice **qué líneas se ejecutaron** durante las pruebas, no **si alguna prueba habría
notado que esas líneas están mal**. Una prueba sin aserciones puede dar 100 % de cobertura.

El *mutation testing* responde la segunda pregunta: PIT introduce pequeños defectos (mutantes) en
el código, por ejemplo cambiar `<` por `<=` o borrar una condición, y vuelve a correr las
pruebas. Si alguna falla, el mutante **muere**; si todas pasan, **sobrevive** y señala una prueba
débil o ausente.

Resultado actual: **39 mutantes, 35 muertos, 1 superviviente y 3 sin cobertura → mutation score 90 %**
(test strength 97 %). Los 3 sin cobertura son los *getters* de `EstudianteDuplicadoException` y
`Estudiante.conId`, que ninguna prueba unitaria invoca; nótese que el servicio tiene 100 % de cobertura de líneas y aun así el número que
importa es este.

El único superviviente es un buen ejemplo de **mutante equivalente**: en `Carrera.desdeTexto`, PIT
elimina la guarda `if (texto == null)`, pero el bucle siguiente hace `name().equals(null)`, que es
`false`, y el método igualmente devuelve `null`. El comportamiento no cambia, así que ninguna
prueba puede matarlo. No todo mutante vivo es una prueba que falta; hay que leerlos.

Reportes: `build/reports/jacoco/combinado/index.html` y `build/reports/pitest/index.html`.

## Pipeline de CI

`.github/workflows/ci.yml` corre en `push` a `main` y a `defecto/**`, en *pull requests* a `main`
y manualmente. Los jobs están **encadenados** (`needs`): si uno falla, los siguientes no corren
(*fail fast*), del más barato al más caro.

```
calidad → unit → integracion → aceptacion → build-imagen → e2e → reportes
```

- `permissions: {}` a nivel de workflow; cada job pide solo lo que usa (`contents: read`, y
  `checks: write` en `reportes` para publicar los resultados JUnit).
- Actions de terceros **fijadas por SHA completo**, con la versión en un comentario; imágenes
  Docker fijadas por digest. Ver [`VERSIONES.md`](VERSIONES.md).
- Caché de Gradle con `gradle/actions/setup-gradle`. Se usa **JDK 25** (el stack es Java 25 LTS).
- Artefactos: reportes HTML y XML de cada nivel, reportes de Cucumber, imagen Docker (entre
  `build-imagen` y `e2e`), traces y capturas de Playwright **cuando E2E falla**, JaCoCo combinado y
  PIT.

## Defectos sembrados

Cuatro ramas `defecto/*` introducen, cada una, un error que atrapa un nivel concreto. En GitHub
Actions se ve qué job se pone en rojo en cada una. Enlaces a los runs y cómo reproducirlos en
[`docs/DEFECTOS.md`](docs/DEFECTOS.md); el análisis por nivel es parte de la
[guía para estudiantes](docs/GUIA-ESTUDIANTE.md).

| Rama | Defecto | Nivel que lo atrapa |
|---|---|---|
| `defecto/unit` | La validación acepta nombres de un carácter | Unit |
| `defecto/integracion` | Error en el mapeo de una columna en el repositorio JDBC | Integración |
| `defecto/aceptacion` | `POST` responde 200 en vez de 201 y sin `Location` | Aceptación |
| `defecto/e2e` | Variable de entorno de la BD mal nombrada en `compose.yaml` | E2E |

## Documentación adicional

| Documento | Contenido |
|---|---|
| [`VERSIONES.md`](VERSIONES.md) | Versión, fecha de publicación y fuente de cada dependencia, plugin, imagen y Action; desvíos justificados |
| [`docs/TDD.md`](docs/TDD.md) | Ciclo rojo → verde → refactor de la regla "nombre único por carrera", con hashes y salida real |
| [`docs/TESTCONTAINERS.md`](docs/TESTCONTAINERS.md) | Por qué PostgreSQL real y no H2 (con diferencias medidas), requisitos en CI, reutilización local |
| [`docs/DEFECTOS.md`](docs/DEFECTOS.md) | Ramas de defectos sembrados con enlaces a los runs de CI |
| [`docs/GUIA-ESTUDIANTE.md`](docs/GUIA-ESTUDIANTE.md) | **Asignación para estudiantes:** guía paso a paso para hacer fork, reproducir cada nivel y cada defecto, y responder preguntas de análisis ([plantilla de entrega](docs/ENTREGA-PLANTILLA.md)) |
