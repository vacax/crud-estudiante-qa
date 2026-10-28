package edu.pucmm.estudiante.repositorio;

import com.zaxxer.hikari.HikariDataSource;
import edu.pucmm.estudiante.config.BaseDatos;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base de las pruebas de integración: un ÚNICO contenedor PostgreSQL compartido por toda la suite.
 *
 * <p>Por qué por suite y no por clase: arrancar PostgreSQL cuesta varios segundos y ese costo se
 * pagaría en cada clase de prueba. A cambio se sacrifica aislamiento, que se recupera con {@code
 * TRUNCATE ... RESTART IDENTITY} antes de cada prueba (barato y determinista). Las pruebas que
 * necesitan una BD realmente virgen (migraciones desde cero) crean una base de datos nueva dentro
 * del mismo contenedor.
 *
 * <p>Patrón singleton estático (en vez de {@code @Testcontainers} + {@code static @Container}, que
 * detiene el contenedor al terminar cada clase): se inicia una vez y Ryuk, el contenedor guardián
 * de Testcontainers, lo elimina al terminar la JVM.
 *
 * <p>Reutilización opcional entre ejecuciones locales (no activada por defecto): agregar {@code
 * .withReuse(true)} al contenedor y definir {@code testcontainers.reuse.enable=true} en {@code
 * ~/.testcontainers.properties} (o {@code TESTCONTAINERS_REUSE_ENABLE=true}). Sin esa habilitación
 * explícita, {@code withReuse} se ignora.
 */
abstract class BasePostgresIT {

  private static final DockerImageName IMAGEN =
      DockerImageName.parse(
              "postgres:18-alpine@sha256:10a5c69a83035ab5239f5620d708520e8b28f5aa1f2b20143e6024532edadfb5")
          .asCompatibleSubstituteFor("postgres");

  static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(IMAGEN);

  static final HikariDataSource DATA_SOURCE;

  static {
    POSTGRES.start();
    DATA_SOURCE =
        BaseDatos.crearDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    BaseDatos.migrar(DATA_SOURCE);
    Runtime.getRuntime().addShutdownHook(new Thread(DATA_SOURCE::close));
  }

  @BeforeEach
  void limpiarTablas() throws SQLException {
    try (Connection cx = DATA_SOURCE.getConnection();
        Statement st = cx.createStatement()) {
      st.execute("TRUNCATE estudiante RESTART IDENTITY");
    }
  }
}
