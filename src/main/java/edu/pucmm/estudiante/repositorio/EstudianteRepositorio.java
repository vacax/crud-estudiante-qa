package edu.pucmm.estudiante.repositorio;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
import java.util.List;
import java.util.Optional;

/** Contrato de persistencia de estudiantes (la implementación JDBC llega en la fase 2). */
public interface EstudianteRepositorio {

  List<Estudiante> listar();

  Optional<Estudiante> buscarPorId(long id);

  /** Inserta y devuelve el estudiante con el id generado. */
  Estudiante guardar(Estudiante estudiante);

  /** Actualiza por id; devuelve {@code true} si existía. */
  boolean actualizar(Estudiante estudiante);

  /** Elimina por id; devuelve {@code true} si existía. */
  boolean eliminar(long id);

  /**
   * Indica si ya existe un estudiante con ese nombre en esa carrera, ignorando al de id {@code
   * excluirId} (puede ser {@code null} al crear).
   */
  boolean existeNombreEnCarrera(String nombre, Carrera carrera, Long excluirId);
}
