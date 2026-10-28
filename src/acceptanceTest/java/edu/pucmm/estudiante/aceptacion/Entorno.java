package edu.pucmm.estudiante.aceptacion;

import com.zaxxer.hikari.HikariDataSource;
import edu.pucmm.estudiante.Aplicacion;
import edu.pucmm.estudiante.config.BaseDatos;
import io.javalin.Javalin;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Entorno compartido por toda la suite: un único PostgreSQL de Testcontainers y la aplicación real
 * levantada en el propio proceso en un puerto aleatorio. Se inicia una vez (singleton estático);
 * Ryuk elimina el contenedor al terminar la JVM.
 */
final class Entorno {

  private static final DockerImageName IMAGEN =
      DockerImageName.parse(
              "postgres:18-alpine@sha256:10a5c69a83035ab5239f5620d708520e8b28f5aa1f2b20143e6024532edadfb5")
          .asCompatibleSubstituteFor("postgres");

  private static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(IMAGEN);
  private static final HikariDataSource DATA_SOURCE;
  private static final Javalin APP;

  static {
    POSTGRES.start();
    DATA_SOURCE =
        BaseDatos.crearDataSource(
            POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    BaseDatos.migrar(DATA_SOURCE);
    APP = Aplicacion.crearApp(DATA_SOURCE).start(0);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  APP.stop();
                  DATA_SOURCE.close();
                }));
  }

  private Entorno() {}

  static int puerto() {
    return APP.port();
  }

  static void limpiarDatos() throws SQLException {
    try (Connection cx = DATA_SOURCE.getConnection();
        Statement st = cx.createStatement()) {
      st.execute("TRUNCATE estudiante RESTART IDENTITY");
    }
  }
}
