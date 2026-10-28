package edu.pucmm.estudiante.dominio;

/** Se lanza cuando no existe un estudiante con el id solicitado (se traduce a HTTP 404). */
public class EstudianteNoEncontradoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final long id;

  public EstudianteNoEncontradoException(long id) {
    super("No existe un estudiante con id " + id);
    this.id = id;
  }

  public long getId() {
    return id;
  }
}
