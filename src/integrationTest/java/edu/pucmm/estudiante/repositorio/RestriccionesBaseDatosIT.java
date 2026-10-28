package edu.pucmm.estudiante.repositorio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Las restricciones viven en la BD: se prueban con SQL crudo, saltándose la validación Java. */
@DisplayName("Restricciones de la base de datos")
class RestriccionesBaseDatosIT extends BasePostgresIT {

  private static final String CHECK_VIOLATION = "23514";
  private static final String NOT_NULL_VIOLATION = "23502";

  private static void insertar(String nombre, String carrera, String correo) throws SQLException {
    try (Connection cx = DATA_SOURCE.getConnection();
        PreparedStatement ps =
            cx.prepareStatement(
                "INSERT INTO estudiante (nombre, carrera, correo) VALUES (?, ?, ?)")) {
      ps.setString(1, nombre);
      ps.setString(2, carrera);
      ps.setString(3, correo);
      ps.executeUpdate();
    }
  }

  private static void esperarViolacion(String sqlState, ThrowingRunnable accion) {
    assertThatThrownBy(accion::run)
        .isInstanceOfSatisfying(
            SQLException.class, e -> assertThat(e.getSQLState()).isEqualTo(sqlState));
  }

  @FunctionalInterface
  private interface ThrowingRunnable {
    void run() throws SQLException;
  }

  @Test
  @DisplayName("un dato válido se inserta sin problema")
  void insercionValida() throws SQLException {
    insertar("Ana Pérez", "ISC", "ana@ejemplo.edu");
  }

  @Test
  @DisplayName("el CHECK de carrera rechaza un código fuera del catálogo")
  void carreraFueraDelCatalogo() {
    esperarViolacion(CHECK_VIOLATION, () -> insertar("Ana Pérez", "XYZ", "ana@ejemplo.edu"));
  }

  @Test
  @DisplayName("NOT NULL rechaza nombre nulo")
  void nombreNulo() {
    esperarViolacion(NOT_NULL_VIOLATION, () -> insertar(null, "ISC", "ana@ejemplo.edu"));
  }

  @Test
  @DisplayName("NOT NULL rechaza carrera nula")
  void carreraNula() {
    esperarViolacion(NOT_NULL_VIOLATION, () -> insertar("Ana Pérez", null, "ana@ejemplo.edu"));
  }

  @Test
  @DisplayName("NOT NULL rechaza correo nulo")
  void correoNulo() {
    esperarViolacion(NOT_NULL_VIOLATION, () -> insertar("Ana Pérez", "ISC", null));
  }

  @Test
  @DisplayName("el CHECK rechaza un nombre de 1 carácter")
  void nombreDeUnCaracter() {
    esperarViolacion(CHECK_VIOLATION, () -> insertar("A", "ISC", "ana@ejemplo.edu"));
  }

  @Test
  @DisplayName("el CHECK acepta un nombre de 2 caracteres (límite)")
  void nombreDeDosCaracteres() throws SQLException {
    insertar("Al", "ISC", "al@ejemplo.edu");
  }

  @Test
  @DisplayName("el CHECK rechaza un nombre con espacios al inicio")
  void nombreConEspacioAlInicio() {
    esperarViolacion(CHECK_VIOLATION, () -> insertar(" Ana Pérez", "ISC", "ana@ejemplo.edu"));
  }

  @Test
  @DisplayName("el CHECK rechaza un nombre con espacios al final")
  void nombreConEspacioAlFinal() {
    esperarViolacion(CHECK_VIOLATION, () -> insertar("Ana Pérez ", "ISC", "ana@ejemplo.edu"));
  }
}
