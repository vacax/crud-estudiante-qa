package edu.pucmm.estudiante.dominio;

/**
 * Un error de validacion asociado a un campo.
 *
 * @param campo nombre del campo invalido
 * @param mensaje descripcion legible del problema
 */
public record ErrorValidacion(String campo, String mensaje) {}
