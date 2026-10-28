package edu.pucmm.estudiante;

import edu.pucmm.estudiante.config.BaseDatos;
import edu.pucmm.estudiante.repositorio.EstudianteRepositorioJdbc;
import edu.pucmm.estudiante.servicio.EstudianteService;
import edu.pucmm.estudiante.web.EstudianteControlador;
import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import io.javalin.http.staticfiles.Location;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import javax.sql.DataSource;

/** Punto de entrada: arma repositorio, servicio y controlador, y arranca Javalin. */
public final class Aplicacion {

  private static final int PUERTO_POR_DEFECTO = 7070;

  private Aplicacion() {}

  public static void main(String[] args) {
    DataSource dataSource = BaseDatos.crearDesdeEntorno();
    BaseDatos.migrar(dataSource);
    String puerto = System.getenv("PUERTO");
    int numero = puerto == null || puerto.isBlank() ? PUERTO_POR_DEFECTO : Integer.parseInt(puerto);
    crearApp(dataSource).start(numero);
  }

  /** Devuelve la aplicación configurada SIN arrancar (las pruebas la inician en el puerto 0). */
  public static Javalin crearApp(DataSource dataSource) {
    var servicio = new EstudianteService(new EstudianteRepositorioJdbc(dataSource));
    var controlador = new EstudianteControlador(servicio);
    return Javalin.create(
        config -> {
          config.staticFiles.add("/public", Location.CLASSPATH);
          controlador.registrar(config.routes);
          config.routes.get("/health", ctx -> responderSalud(ctx, dataSource));
        });
  }

  private static void responderSalud(io.javalin.http.Context ctx, DataSource dataSource) {
    try (Connection cx = dataSource.getConnection();
        Statement st = cx.createStatement()) {
      st.execute("SELECT 1");
      ctx.status(HttpStatus.OK).json(Map.of("estado", "UP"));
    } catch (Exception e) {
      ctx.status(HttpStatus.SERVICE_UNAVAILABLE).json(Map.of("estado", "DOWN"));
    }
  }
}
