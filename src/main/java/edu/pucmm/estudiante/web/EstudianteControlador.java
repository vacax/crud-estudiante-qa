package edu.pucmm.estudiante.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.pucmm.estudiante.dominio.ErrorValidacion;
import edu.pucmm.estudiante.dominio.EstudianteNoEncontradoException;
import edu.pucmm.estudiante.dominio.EstudianteSolicitud;
import edu.pucmm.estudiante.dominio.ValidacionException;
import edu.pucmm.estudiante.servicio.EstudianteService;
import io.javalin.config.RoutesConfig;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import java.util.List;
import java.util.Map;

/** Capa web: traduce HTTP a llamadas del servicio y las excepciones a respuestas uniformes. */
public class EstudianteControlador {

  public static final String RUTA = "/api/estudiantes";

  private final EstudianteService servicio;

  public EstudianteControlador(EstudianteService servicio) {
    this.servicio = servicio;
  }

  /** Registra rutas y manejadores de excepciones (API de Javalin 7: {@code config.routes}). */
  public void registrar(RoutesConfig rutas) {
    rutas.get(RUTA, this::listar);
    rutas.get(RUTA + "/{id}", this::obtener);
    rutas.post(RUTA, this::crear);
    rutas.put(RUTA + "/{id}", this::actualizar);
    rutas.delete(RUTA + "/{id}", this::eliminar);

    rutas.exception(
        ValidacionException.class,
        (e, ctx) -> responder(ctx, HttpStatus.BAD_REQUEST, e.getErrores()));
    rutas.exception(
        EstudianteNoEncontradoException.class,
        (e, ctx) ->
            responder(
                ctx, HttpStatus.NOT_FOUND, List.of(new ErrorValidacion("id", e.getMessage()))));
    rutas.exception(
        JsonProcessingException.class,
        (e, ctx) ->
            responder(
                ctx,
                HttpStatus.BAD_REQUEST,
                List.of(new ErrorValidacion("cuerpo", "El cuerpo no es un JSON válido"))));
    rutas.exception(
        Exception.class,
        (e, ctx) ->
            responder(
                ctx,
                HttpStatus.INTERNAL_SERVER_ERROR,
                List.of(new ErrorValidacion("servidor", "Error interno del servidor"))));
  }

  private void listar(Context ctx) {
    ctx.json(servicio.listar());
  }

  private void obtener(Context ctx) {
    ctx.json(servicio.obtener(id(ctx)));
  }

  private void crear(Context ctx) {
    var creado = servicio.crear(cuerpo(ctx));
    ctx.status(HttpStatus.CREATED).header("Location", RUTA + "/" + creado.id()).json(creado);
  }

  private void actualizar(Context ctx) {
    ctx.json(servicio.actualizar(id(ctx), cuerpo(ctx)));
  }

  private void eliminar(Context ctx) {
    servicio.eliminar(id(ctx));
    ctx.status(HttpStatus.NO_CONTENT);
  }

  private static long id(Context ctx) {
    try {
      return Long.parseLong(ctx.pathParam("id"));
    } catch (NumberFormatException e) {
      throw new ValidacionException(List.of(new ErrorValidacion("id", "El id debe ser numérico")));
    }
  }

  private static EstudianteSolicitud cuerpo(Context ctx) {
    EstudianteSolicitud solicitud = ctx.bodyAsClass(EstudianteSolicitud.class);
    if (solicitud == null) {
      throw new ValidacionException(
          List.of(new ErrorValidacion("cuerpo", "El cuerpo de la solicitud es obligatorio")));
    }
    return solicitud;
  }

  private static void responder(Context ctx, HttpStatus estado, List<ErrorValidacion> errores) {
    ctx.status(estado).json(Map.of("errores", errores));
  }
}
