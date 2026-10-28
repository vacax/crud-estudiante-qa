package edu.pucmm.estudiante.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

/** Configuración de la conexión a PostgreSQL (pool HikariCP) y de las migraciones Flyway. */
public final class BaseDatos {

  /** Ubicación de las migraciones dentro del classpath. */
  public static final String UBICACION_MIGRACIONES = "classpath:db/migration";

  private BaseDatos() {}

  /**
   * Crea el pool a partir de las variables de entorno {@code DB_URL}, {@code DB_USUARIO} y {@code
   * DB_CLAVE}.
   */
  public static HikariDataSource crearDesdeEntorno() {
    return crearDataSource(requerida("DB_URL"), requerida("DB_USUARIO"), requerida("DB_CLAVE"));
  }

  /** Crea el pool con valores explícitos (lo usan las pruebas con Testcontainers). */
  public static HikariDataSource crearDataSource(String url, String usuario, String clave) {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(url);
    config.setUsername(usuario);
    config.setPassword(clave);
    config.setMaximumPoolSize(5);
    return new HikariDataSource(config);
  }

  /** Aplica las migraciones pendientes y devuelve el número de migraciones ejecutadas. */
  public static int migrar(DataSource dataSource) {
    return Flyway.configure()
        .dataSource(dataSource)
        .locations(UBICACION_MIGRACIONES)
        .load()
        .migrate()
        .migrationsExecuted;
  }

  private static String requerida(String nombre) {
    String valor = System.getenv(nombre);
    if (valor == null || valor.isBlank()) {
      throw new IllegalStateException("Falta la variable de entorno " + nombre);
    }
    return valor;
  }
}
