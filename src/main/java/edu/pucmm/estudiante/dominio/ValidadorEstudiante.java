package edu.pucmm.estudiante.dominio;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Reglas de validacion de {@link EstudianteSolicitud}. Acumula todos los errores encontrados. */
public final class ValidadorEstudiante {

  public static final int NOMBRE_MIN = 2;
  public static final int NOMBRE_MAX = 100;

  /**
   * Regex de correo deliberadamente sencilla (no cubre todo el RFC 5322): parte local con letras,
   * digitos y {@code . _ % + -}; arroba; dominio con etiquetas separadas por punto y un TLD de al
   * menos 2 letras. Sin espacios.
   */
  static final Pattern CORREO =
      Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

  private ValidadorEstudiante() {}

  /** Devuelve la lista de errores (vacia si la solicitud es valida). */
  public static List<ErrorValidacion> validar(EstudianteSolicitud solicitud) {
    List<ErrorValidacion> errores = new ArrayList<>();
    validarNombre(solicitud.nombre(), errores);
    validarCarrera(solicitud.carrera(), errores);
    validarCorreo(solicitud.correo(), errores);
    return errores;
  }

  /** Valida y, si hay errores, lanza {@link ValidacionException}. */
  public static void validarOLanzar(EstudianteSolicitud solicitud) {
    List<ErrorValidacion> errores = validar(solicitud);
    if (!errores.isEmpty()) {
      throw new ValidacionException(errores);
    }
  }

  private static void validarNombre(String nombre, List<ErrorValidacion> errores) {
    if (nombre == null || nombre.isEmpty()) {
      errores.add(new ErrorValidacion("nombre", "El nombre es obligatorio"));
      return;
    }
    if (!nombre.equals(nombre.strip())) {
      errores.add(
          new ErrorValidacion("nombre", "El nombre no debe tener espacios al inicio ni al final"));
    }
    if (nombre.length() < NOMBRE_MIN || nombre.length() > NOMBRE_MAX) {
      errores.add(
          new ErrorValidacion(
              "nombre",
              "El nombre debe tener entre " + NOMBRE_MIN + " y " + NOMBRE_MAX + " caracteres"));
    }
  }

  private static void validarCarrera(String carrera, List<ErrorValidacion> errores) {
    if (carrera == null || carrera.isEmpty()) {
      errores.add(new ErrorValidacion("carrera", "La carrera es obligatoria"));
    } else if (Carrera.desdeTexto(carrera) == null) {
      errores.add(
          new ErrorValidacion("carrera", "La carrera no pertenece al catalogo: " + carrera));
    }
  }

  private static void validarCorreo(String correo, List<ErrorValidacion> errores) {
    if (correo == null || correo.isEmpty()) {
      errores.add(new ErrorValidacion("correo", "El correo es obligatorio"));
    } else if (!CORREO.matcher(correo).matches()) {
      errores.add(new ErrorValidacion("correo", "El correo no tiene un formato valido"));
    }
  }
}
