package edu.pucmm.estudiante.repositorio;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
import edu.pucmm.estudiante.dominio.EstudianteDuplicadoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/** Implementación JDBC pura (sin ORM) de {@link EstudianteRepositorio} sobre PostgreSQL. */
public class EstudianteRepositorioJdbc implements EstudianteRepositorio {

  private static final String SQLSTATE_UNICA = "23505";
  private static final String RESTRICCION_NOMBRE_CARRERA = "uq_estudiante_nombre_carrera";

  private static final String SELECT = "SELECT id, nombre, carrera, correo FROM estudiante";

  private final DataSource dataSource;

  public EstudianteRepositorioJdbc(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public List<Estudiante> listar() {
    try (Connection cx = dataSource.getConnection();
        PreparedStatement ps = cx.prepareStatement(SELECT + " ORDER BY id");
        ResultSet rs = ps.executeQuery()) {
      List<Estudiante> resultado = new ArrayList<>();
      while (rs.next()) {
        resultado.add(mapear(rs));
      }
      return resultado;
    } catch (SQLException e) {
      throw new RepositorioException("No se pudo listar los estudiantes", e);
    }
  }

  @Override
  public Optional<Estudiante> buscarPorId(long id) {
    try (Connection cx = dataSource.getConnection();
        PreparedStatement ps = cx.prepareStatement(SELECT + " WHERE id = ?")) {
      ps.setLong(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
      }
    } catch (SQLException e) {
      throw new RepositorioException("No se pudo buscar el estudiante " + id, e);
    }
  }

  @Override
  public Estudiante guardar(Estudiante estudiante) {
    String sql = "INSERT INTO estudiante (nombre, carrera, correo) VALUES (?, ?, ?) RETURNING id";
    try (Connection cx = dataSource.getConnection();
        PreparedStatement ps = cx.prepareStatement(sql)) {
      ps.setString(1, estudiante.nombre());
      ps.setString(2, estudiante.carrera().name());
      ps.setString(3, estudiante.correo());
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return estudiante.conId(rs.getLong("id"));
      }
    } catch (SQLException e) {
      traducirDuplicado(e, estudiante);
      throw new RepositorioException("No se pudo guardar el estudiante", e);
    }
  }

  @Override
  public boolean actualizar(Estudiante estudiante) {
    String sql = "UPDATE estudiante SET nombre = ?, carrera = ?, correo = ? WHERE id = ?";
    try (Connection cx = dataSource.getConnection();
        PreparedStatement ps = cx.prepareStatement(sql)) {
      ps.setString(1, estudiante.nombre());
      ps.setString(2, estudiante.carrera().name());
      ps.setString(3, estudiante.correo());
      ps.setLong(4, estudiante.id());
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      traducirDuplicado(e, estudiante);
      throw new RepositorioException("No se pudo actualizar el estudiante " + estudiante.id(), e);
    }
  }

  @Override
  public boolean eliminar(long id) {
    try (Connection cx = dataSource.getConnection();
        PreparedStatement ps = cx.prepareStatement("DELETE FROM estudiante WHERE id = ?")) {
      ps.setLong(1, id);
      return ps.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new RepositorioException("No se pudo eliminar el estudiante " + id, e);
    }
  }

  @Override
  public boolean existeNombreEnCarrera(String nombre, Carrera carrera, Long excluirId) {
    String sql =
        "SELECT EXISTS (SELECT 1 FROM estudiante WHERE nombre = ? AND carrera = ?"
            + " AND id IS DISTINCT FROM ?)";
    try (Connection cx = dataSource.getConnection();
        PreparedStatement ps = cx.prepareStatement(sql)) {
      ps.setString(1, nombre);
      ps.setString(2, carrera.name());
      ps.setObject(3, excluirId);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getBoolean(1);
      }
    } catch (SQLException e) {
      throw new RepositorioException("No se pudo verificar el nombre duplicado", e);
    }
  }

  /** Si la BD rechazó el duplicado (p. ej. por concurrencia), lo expresa como regla de negocio. */
  private static void traducirDuplicado(SQLException e, Estudiante estudiante) {
    if (SQLSTATE_UNICA.equals(e.getSQLState())
        && e.getMessage() != null
        && e.getMessage().contains(RESTRICCION_NOMBRE_CARRERA)) {
      throw new EstudianteDuplicadoException(estudiante.nombre(), estudiante.carrera());
    }
  }

  private Estudiante mapear(ResultSet rs) throws SQLException {
    return new Estudiante(
        rs.getLong("id"),
        rs.getString("nombre"),
        Carrera.valueOf(rs.getString("carrera")),
        rs.getString("correo"));
  }
}
