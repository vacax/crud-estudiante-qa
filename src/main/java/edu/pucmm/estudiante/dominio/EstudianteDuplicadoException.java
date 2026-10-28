package edu.pucmm.estudiante.dominio;

/** Se lanza al intentar repetir un nombre dentro de la misma carrera (se traduce a HTTP 409). */
public class EstudianteDuplicadoException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String nombre;
  private final Carrera carrera;

  public EstudianteDuplicadoException(String nombre, Carrera carrera) {
    super("Ya existe un estudiante llamado " + nombre + " en la carrera " + carrera);
    this.nombre = nombre;
    this.carrera = carrera;
  }

  public String getNombre() {
    return nombre;
  }

  public Carrera getCarrera() {
    return carrera;
  }
}
