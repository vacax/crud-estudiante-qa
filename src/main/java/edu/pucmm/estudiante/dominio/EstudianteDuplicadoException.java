package edu.pucmm.estudiante.dominio;

/** Se lanza al intentar repetir un nombre dentro de la misma carrera (se traduce a HTTP 409). */
public class EstudianteDuplicadoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public EstudianteDuplicadoException(String mensaje) {
    super(mensaje);
  }
}
