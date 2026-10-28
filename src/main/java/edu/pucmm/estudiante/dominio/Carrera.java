package edu.pucmm.estudiante.dominio;

/**
 * Catálogo cerrado de carreras.
 *
 * <p>Nota: el catálogo es didáctico; no representa la oferta académica real de ninguna institución.
 */
public enum Carrera {
  ISC,
  ITT,
  IME,
  IIS,
  ICI;

  /**
   * Convierte un texto a {@link Carrera} de forma estricta (distingue mayúsculas).
   *
   * @param texto valor recibido, puede ser null
   * @return la carrera, o {@code null} si el texto es null o no pertenece al catálogo
   */
  public static Carrera desdeTexto(String texto) {
    if (texto == null) {
      return null;
    }
    for (Carrera carrera : values()) {
      if (carrera.name().equals(texto)) {
        return carrera;
      }
    }
    return null;
  }
}
