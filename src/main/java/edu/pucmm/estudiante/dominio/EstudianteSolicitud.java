package edu.pucmm.estudiante.dominio;

/**
 * Comando de entrada (crear o actualizar). Todos los campos llegan como texto crudo, tal como los
 * recibe la API, y se validan con {@link ValidadorEstudiante}.
 *
 * @param nombre nombre recibido
 * @param carrera código de carrera recibido
 * @param correo correo recibido
 */
public record EstudianteSolicitud(String nombre, String carrera, String correo) {}
