package edu.pucmm.estudiante.repositorio;

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
}
