package edu.pucmm.estudiante.pruebas;

import edu.pucmm.estudiante.dominio.Carrera;
import edu.pucmm.estudiante.dominio.Estudiante;
import edu.pucmm.estudiante.dominio.EstudianteSolicitud;

/**
 * Test data builder determinista: valores por defecto fijos (sin azar) que se sobrescriben solo en
 * lo que cada prueba quiere resaltar.
 */
public final class EstudianteBuilder {

  private Long id = null;
  private String nombre = "Ana Perez";
  private String carrera = Carrera.ISC.name();
  private String correo = "ana.perez@ejemplo.edu";

  private EstudianteBuilder() {}

  public static EstudianteBuilder unEstudiante() {
    return new EstudianteBuilder();
  }

  public EstudianteBuilder conId(Long id) {
    this.id = id;
    return this;
  }

  public EstudianteBuilder conNombre(String nombre) {
    this.nombre = nombre;
    return this;
  }

  public EstudianteBuilder conCarrera(String carrera) {
    this.carrera = carrera;
    return this;
  }

  public EstudianteBuilder conCorreo(String correo) {
    this.correo = correo;
    return this;
  }

  public EstudianteSolicitud solicitud() {
    return new EstudianteSolicitud(nombre, carrera, correo);
  }

  /** Construye la entidad; la carrera debe pertenecer al catalogo. */
  public Estudiante construir() {
    return new Estudiante(id, nombre, Carrera.desdeTexto(carrera), correo);
  }
}
