package edu.pucmm.estudiante.dominio;

/**
 * Estudiante ya validado.
 *
 * @param id identificador generado por la base de datos; {@code null} antes de persistir
 * @param nombre nombre completo
 * @param carrera carrera del catálogo
 * @param correo correo electrónico
 */
public record Estudiante(Long id, String nombre, Carrera carrera, String correo) {

  /** Devuelve una copia con el id indicado. */
  public Estudiante conId(Long nuevoId) {
    return new Estudiante(nuevoId, nombre, carrera, correo);
  }
}
