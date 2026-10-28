package edu.pucmm.estudiante.dominio;

import java.util.List;

/** Se lanza cuando una solicitud incumple una o más reglas; contiene TODOS los errores. */
public class ValidacionException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final transient List<ErrorValidacion> errores;

  public ValidacionException(List<ErrorValidacion> errores) {
    super("La solicitud tiene " + errores.size() + " error(es) de validación");
    this.errores = List.copyOf(errores);
  }

  public List<ErrorValidacion> getErrores() {
    return errores;
  }
}
