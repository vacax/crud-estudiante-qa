package edu.pucmm.estudiante.dominio;

/**
 * Un error de validación asociado a un campo.
 *
 * @param campo nombre del campo inválido
 * @param mensaje descripción legible del problema
 */
public record ErrorValidacion(String campo, String mensaje) {}
