package edu.pucmm.estudiante.dominio;

/**
 * Catalogo cerrado de carreras.
 *
 * <p>Nota: el catalogo es didactico; no representa la oferta academica real de ninguna institucion.
 */
public enum Carrera {
  ISC,
  ITT,
  IME,
  IIS,
  ICI;

  /**
   * Convierte un texto a {@link Carrera} de forma estricta (distingue mayusculas).
   *
   * @param texto valor recibido, puede ser null
   * @return la carrera, o {@code null} si el texto es null o no pertenece al catalogo
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
