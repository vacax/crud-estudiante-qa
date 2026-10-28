package edu.pucmm.estudiante.repositorio;

import static org.assertj.core.api.Assertions.assertThat;

import com.zaxxer.hikari.HikariDataSource;
import edu.pucmm.estudiante.config.BaseDatos;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Migraciones Flyway")
class MigracionesIT extends BasePostgresIT {

  @Test
  @DisplayName("se aplican desde cero en una base de datos nueva y quedan registradas con éxito")
  void seAplicanDesdeCero() throws SQLException {
    try (Connection admin = DATA_SOURCE.getConnection();
        Statement st = admin.createStatement()) {
      st.execute("CREATE DATABASE virgen");
    }
    String urlVirgen = POSTGRES.getJdbcUrl().replaceFirst("/[^/?]+(\\?|$)", "/virgen$1");

    try (HikariDataSource ds =
        BaseDatos.crearDataSource(urlVirgen, POSTGRES.getUsername(), POSTGRES.getPassword())) {
      int ejecutadas = BaseDatos.migrar(ds);

      assertThat(ejecutadas).isEqualTo(2);

      var aplicadas =
          Flyway.configure()
              .dataSource(ds)
              .locations(BaseDatos.UBICACION_MIGRACIONES)
              .load()
              .info()
              .applied();
      assertThat(aplicadas).hasSize(2);
      assertThat(aplicadas[0].getVersion().getVersion()).isEqualTo("1");
      assertThat(aplicadas[1].getVersion().getVersion()).isEqualTo("2");
      assertThat(aplicadas[0].getState()).isEqualTo(MigrationState.SUCCESS);
      assertThat(aplicadas[1].getState()).isEqualTo(MigrationState.SUCCESS);

      try (Connection cx = ds.getConnection();
          Statement s = cx.createStatement();
          ResultSet rs =
              s.executeQuery(
                  "SELECT version, success FROM flyway_schema_history WHERE version IS NOT NULL")) {
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString("version")).isEqualTo("1");
        assertThat(rs.getBoolean("success")).isTrue();
        assertThat(rs.next()).isTrue();
        assertThat(rs.getString("version")).isEqualTo("2");
        assertThat(rs.getBoolean("success")).isTrue();
        assertThat(rs.next()).isFalse();
      }
    }
  }

  @Test
  @DisplayName("migrar de nuevo es idempotente: no ejecuta nada")
  void migrarDeNuevoNoHaceNada() {
    assertThat(BaseDatos.migrar(DATA_SOURCE)).isZero();
  }
}
