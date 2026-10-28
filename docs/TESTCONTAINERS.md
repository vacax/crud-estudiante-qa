# Testcontainers en este proyecto

Testcontainers es una biblioteca de Java que **arranca contenedores Docker desde el propio código de
prueba** y los elimina al terminar. Aquí lo usamos para que las pruebas hablen con un **PostgreSQL
real** (la misma imagen que se despliega) en lugar de una base de datos simulada.

Versión: Testcontainers **2.0.5** (artefactos `testcontainers-postgresql` y
`testcontainers-junit-jupiter`; ver `VERSIONES.md`).

## Dónde se usa

| Nivel | Qué levanta | Ciclo de vida | Clase |
|---|---|---|---|
| Integración | Un `PostgreSQLContainer` | Singleton por suite | `BasePostgresIT` |
| Aceptación | Un `PostgreSQLContainer` + la aplicación en el propio proceso | Singleton por suite | `Entorno` |
| E2E | `ComposeContainer` sobre el **mismo `compose.yaml`** (app + PostgreSQL) | Una vez por clase | `BaseE2E` |

### Integración: singleton por suite

```java
private static final DockerImageName IMAGEN =
    DockerImageName.parse(
            "postgres:18-alpine@sha256:10a5c69a83035ab5239f5620d708520e8b28f5aa1f2b20143e6024532edadfb5")
        .asCompatibleSubstituteFor("postgres");

static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(IMAGEN);

static {
  POSTGRES.start();
  DATA_SOURCE =
      BaseDatos.crearDataSource(
          POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
  BaseDatos.migrar(DATA_SOURCE);
}

@BeforeEach
void limpiarTablas() throws SQLException {
  try (Connection cx = DATA_SOURCE.getConnection();
      Statement st = cx.createStatement()) {
    st.execute("TRUNCATE estudiante RESTART IDENTITY");
  }
}
```

**Por qué así.** Arrancar PostgreSQL cuesta varios segundos; hacerlo por clase de prueba se pagaría
muchas veces. Con un único contenedor por suite el costo se paga una sola vez. A cambio se pierde
aislamiento (todas las pruebas comparten la base), y ese aislamiento se recupera **antes de cada
prueba** con `TRUNCATE estudiante RESTART IDENTITY`: es casi instantáneo, determinista y reinicia el
`id` generado, de modo que las pruebas pueden afirmar `id = 1`. Las pruebas que necesitan una base
realmente virgen (migraciones desde cero) crean una base de datos nueva dentro del mismo contenedor.

El patrón es un **singleton estático** y no `@Testcontainers` + `@Container static`, porque esta
combinación detiene el contenedor al terminar cada clase. Con el singleton, quien lo elimina al
final es **Ryuk**, el contenedor guardián de Testcontainers, cuando termina la JVM.

### Aceptación: singleton + aplicación en proceso

```java
static {
  POSTGRES.start();
  DATA_SOURCE = BaseDatos.crearDataSource(/* url, usuario y clave del contenedor */);
  BaseDatos.migrar(DATA_SOURCE);
  APP = Aplicacion.crearApp(DATA_SOURCE).start(0);   // puerto aleatorio
}
```

Los pasos de Cucumber usan REST Assured contra `APP.port()`. La aplicación corre dentro de la JVM de
prueba (arranca en milisegundos) y solo la base de datos va en contenedor. Se limpia con el mismo
`TRUNCATE ... RESTART IDENTITY` entre escenarios (`Entorno.limpiarDatos()`).

### E2E: `ComposeContainer` con el `compose.yaml` real

```java
stack =
    new ComposeContainer(compose)
        // Puerto del host aleatorio: no choca con nada que ya escuche en 7070.
        .withEnv("APP_PUERTO", "0")
        .withExposedService(
            "app", 7070,
            Wait.forHttp("/health").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(3)))
        .withStartupTimeout(Duration.ofMinutes(3));
stack.start();
```

Aquí se levanta el stack **tal como se despliega**: la imagen de la app construida con el
`Dockerfile` más PostgreSQL, ambos definidos en `compose.yaml`. Es la estrategia más lenta
(construye/arranca dos contenedores), por eso solo se usa en E2E y se comparte entre las pruebas de
cada clase. Con Testcontainers 2.x `ComposeContainer` usa el CLI local `docker compose`, así que ese
comando debe existir donde corran las pruebas.

## Por qué es mejor que H2 u otra base en memoria

Una base en memoria es rápida, pero es **otro motor** con otro comportamiento. Una prueba que pasa en
H2 no demuestra que el código funcione en PostgreSQL, y es en PostgreSQL donde corre en producción.
Las diferencias típicas: códigos de error (`SQLState`), funciones y tipos propios, reglas de
`IDENTITY`, comparación de texto, concurrencia y bloqueos, y extensiones del motor.

### Ejemplo verificado: nuestro propio esquema, dos motores

Se aplicó **la migración real** `V1__crear_tabla_estudiante.sql` en H2 2.5.252 con
`MODE=PostgreSQL` y en PostgreSQL (`postgres:18-alpine` con el digest del proyecto), y se hicieron los
mismos `INSERT` que violan las reglas del esquema. Se registró el `SQLState` que devuelve cada motor.

| Sentencia | PostgreSQL 18 | H2 (`MODE=PostgreSQL`) |
|---|---|---|
| `nombre` de 1 carácter (`CHECK char_length >= 2`) | `23514` | `23513` |
| `carrera = 'XYZ'` (`CHECK ... IN (...)`) | `23514` | `23513` |
| `nombre = ' Ana'` (`CHECK nombre = btrim(nombre)`) | `23514` | `23513` |
| `INSERT` con `id` explícito en `GENERATED ALWAYS AS IDENTITY` | `428C9` | `90154` |
| `carrera = 'ISCX'` en `VARCHAR(3)` | `22001` | `22001` (coincide) |

Nuestras pruebas de integración (`RestriccionesBaseDatosIT`) comprueban que `getSQLState()` sea
`23514` ante una violación de `CHECK`. **Contra H2 esas pruebas fallarían** (llegaría `23513`) aunque
el esquema estuviera bien; y si "arregláramos" la prueba para H2, dejaría de reflejar lo que ocurre en
PostgreSQL. La última fila muestra que no todo difiere, y justamente por eso no se sabe cuáles
diferencias hay hasta que se prueba contra el motor real.

#### Salida real en H2

Programa (`Demo.java`, un solo archivo) ejecutado con `java -cp h2-2.5.252.jar Demo.java <V1.sql>`;
la conexión es `jdbc:h2:mem:demo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE`:

```text
Migración V1 aplicada en H2: OK
nombre de 1 caracter (CHECK)       -> SQLState 23513
carrera 'XYZ' (CHECK)              -> SQLState 23513
nombre ' Ana' con espacio (CHECK)  -> SQLState 23513
id explícito (ALWAYS IDENTITY)     -> SQLState 90154
carrera de 4 caracteres (VARCHAR(3)) -> SQLState 22001
```

El núcleo del programa es solo esto (el resto es `probar(...)` repetido con cada `INSERT`):

```java
try (Connection cx = DriverManager.getConnection(
        "jdbc:h2:mem:demo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE")) {
  cx.createStatement().execute(Files.readString(Path.of(args[0])));   // la migración V1 real
  try {
    cx.createStatement().execute(
        "INSERT INTO estudiante(nombre,carrera,correo) VALUES ('A','ISC','a@x.com')");
  } catch (SQLException e) {
    System.out.println("SQLState " + e.getSQLState());
  }
}
```

#### Salida real en PostgreSQL

La misma migración seguida de los mismos `INSERT`, con `psql` y `\set VERBOSITY sqlstate`, dentro de
un contenedor `postgres:18-alpine@sha256:10a5c69a...edadfb5`:

```text
CREATE TABLE
nombre de 1 caracter (CHECK)
psql:/d/pg_all.sql:14: ERROR:  23514
carrera XYZ (CHECK)
psql:/d/pg_all.sql:16: ERROR:  23514
nombre  Ana con espacio (CHECK)
psql:/d/pg_all.sql:18: ERROR:  23514
id explicito (ALWAYS IDENTITY)
psql:/d/pg_all.sql:20: ERROR:  428C9
carrera de 4 caracteres (VARCHAR 3)
psql:/d/pg_all.sql:22: ERROR:  22001
```

(`psql` interpreta las comillas dentro de `\echo`, por eso en las etiquetas faltan las comillas
simples; no afecta a los `INSERT`.)

Para reproducirlo, descargar `com.h2database:h2:2.5.252` de Maven Central (publicado el 2026-09-26) y
ejecutar el programa contra `src/main/resources/db/migration/V1__crear_tabla_estudiante.sql`.

### Resumen de ventajas de Testcontainers

- **Mismo motor y misma versión que producción** (mismo digest que `compose.yaml`).
- Las **migraciones de Flyway** se prueban tal como se aplican de verdad, sin dialectos de
  compatibilidad.
- Los códigos de error y el comportamiento de las restricciones son los reales.
- Sin configuración previa en la máquina: solo hace falta Docker.

El costo: las pruebas son más lentas que con una base en memoria (arranque del contenedor) y exigen
Docker. Por eso el contenedor se comparte por suite y los datos se limpian con `TRUNCATE`.

## Qué requiere en CI

- **Un daemon Docker accesible** para el proceso de pruebas. Los runners `ubuntu-latest` de GitHub
  Actions ya traen Docker, así que `./gradlew integrationTest acceptanceTest e2eTest` funciona sin
  pasos adicionales de configuración de Docker.
- **Ryuk** (contenedor guardián) se arranca solo y elimina los contenedores aunque la JVM muera. Se
  puede desactivar con la variable `TESTCONTAINERS_RYUK_DISABLED=true`, pero no se recomienda: en un
  runner efímero no importa, y en una máquina compartida dejaría contenedores huérfanos.
- **No funciona en runners sin Docker**, por ejemplo los macOS hospedados de GitHub Actions. En esos
  casos hay que usar otro runner o un Docker remoto.
- **Alternativas:** Testcontainers Cloud o un host Docker remoto. VERIFICAR: se mencionan solo como
  alternativas; en este proyecto no se probaron ni se documenta su configuración.
- La E2E además necesita el plugin `docker compose` y poder **construir la imagen** de la app (el
  `Dockerfile` descarga la distribución de Gradle y las dependencias, por lo que necesita red).

## Reutilizar contenedores en desarrollo local

Por defecto cada ejecución crea un contenedor nuevo y lo borra al terminar. Para acelerar el ciclo
local se puede **conservar** el contenedor entre ejecuciones:

1. Habilitar la reutilización en la máquina del desarrollador, en `~/.testcontainers.properties`:

   ```properties
   testcontainers.reuse.enable=true
   ```

   (También sirve la variable de entorno `TESTCONTAINERS_REUSE_ENABLE=true`.) El nombre de la
   propiedad se confirmó en el JAR 2.0.5 (`TestcontainersConfiguration` contiene la cadena
   `testcontainers.reuse.enable`).

2. Marcar el contenedor en el código:

   ```java
   static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(IMAGEN).withReuse(true);
   ```

Sin el paso 1, `withReuse(true)` **se ignora**. Esto es intencional: el repositorio no activa nada
por sí solo y el CI sigue creando contenedores nuevos. Por eso `BasePostgresIT` no llama a
`withReuse` y solo lo menciona en un comentario.

**Advertencias**

- **No se limpia solo.** Un contenedor reutilizado queda corriendo después de las pruebas y Ryuk no lo
  elimina. Hay que borrarlo a mano (`docker ps`, `docker rm -f <id>`).
- **No usarlo en CI.** Allí se quiere un entorno limpio y los runners son efímeros.
- **Los datos persisten entre ejecuciones.** Nuestro `TRUNCATE ... RESTART IDENTITY` antes de cada
  prueba lo mitiga para la tabla `estudiante`, pero otras bases/tablas creadas por las pruebas, y el
  historial de migraciones de Flyway, se conservan: una prueba de "migrar desde cero" debe crear su
  propia base de datos.
- Si cambias la imagen o la configuración del contenedor, Testcontainers creará otro; el anterior
  queda huérfano.
- VERIFICAR: la documentación oficial (https://java.testcontainers.org) presenta la reutilización
  como característica experimental y con limitaciones; no se comprobó en esta fase si aplica a
  `ComposeContainer`, por lo que aquí solo se propone para el contenedor de PostgreSQL.
