package edu.pucmm.estudiante.repositorio;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
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

  private Estudiante mapear(ResultSet rs) throws SQLException {
    return new Estudiante(
        rs.getLong("id"),
        rs.getString("nombre"),
        Carrera.valueOf(rs.getString("carrera")),
        rs.getString("correo"));
  }
}
