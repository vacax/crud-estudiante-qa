package edu.pucmm.estudiante.repositorio;

/** Error de acceso a datos; envuelve la {@link java.sql.SQLException} original. */
public class RepositorioException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public RepositorioException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
