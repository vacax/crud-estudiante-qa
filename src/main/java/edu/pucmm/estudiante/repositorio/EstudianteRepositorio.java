package edu.pucmm.estudiante.repositorio;

import edu.pucmm.estudiante.dominio.Estudiante;
import java.util.List;
import java.util.Optional;

/** Contrato de persistencia de estudiantes (la implementacion JDBC llega en la fase 2). */
public interface EstudianteRepositorio {

  List<Estudiante> listar();

  Optional<Estudiante> buscarPorId(long id);

  /** Inserta y devuelve el estudiante con el id generado. */
  Estudiante guardar(Estudiante estudiante);

  /** Actualiza por id; devuelve {@code true} si existia. */
  boolean actualizar(Estudiante estudiante);

  /** Elimina por id; devuelve {@code true} si existia. */
  boolean eliminar(long id);
}
