package edu.pucmm.estudiante.aceptacion;

import static edu.pucmm.estudiante.pruebas.EstudianteBuilder.unEstudiante;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import io.cucumber.java.Before;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.cucumber.java.es.Y;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.sql.SQLException;

/** Pasos en español; cada uno habla con la aplicación real por HTTP (REST Assured). */
public class PasosEstudiante {

  /** En los ejemplos, "[espacio]" representa un espacio (las celdas de tabla se recortan). */
  private static final String MARCA_ESPACIO = "[espacio]";

  private Response ultimaRespuesta;
  private String ubicacionCreado;

  @Before
  public void limpiarDatos() throws SQLException {
    Entorno.limpiarDatos();
    ultimaRespuesta = null;
    ubicacionCreado = null;
  }

  private static RequestSpecification peticion() {
    return given().port(Entorno.puerto()).contentType(ContentType.JSON);
  }

  private static String texto(String valor) {
    return valor.replace(MARCA_ESPACIO, " ");
  }

  // ---- Dado ----

  @Dado("que no hay estudiantes registrados")
  public void queNoHayEstudiantesRegistrados() {
    peticion()
        .get("/api/estudiantes")
        .then()
        .statusCode(200)
        .body("size()", org.hamcrest.Matchers.is(0));
  }

  @Dado("que existe el estudiante {string} de la carrera {string}")
  public void queExisteElEstudiante(String nombre, String carrera) {
    Response r =
        peticion()
            .body(unEstudiante().conNombre(nombre).conCarrera(carrera).solicitud())
            .post("/api/estudiantes");
    assertThat(r.statusCode()).isEqualTo(201);
    ubicacionCreado = r.header("Location");
  }

  // ---- Cuando ----

  @Cuando("registro al estudiante {string} de la carrera {string} con correo {string}")
  public void registroAlEstudiante(String nombre, String carrera, String correo) {
    intentoRegistrar(nombre, carrera, correo);
  }

  @Cuando("intento registrar un estudiante con nombre {string}, carrera {string} y correo {string}")
  public void intentoRegistrar(String nombre, String carrera, String correo) {
    ultimaRespuesta =
        peticion()
            .body(
                unEstudiante()
                    .conNombre(texto(nombre))
                    .conCarrera(carrera)
                    .conCorreo(correo)
                    .solicitud())
            .post("/api/estudiantes");
    ubicacionCreado = ultimaRespuesta.header("Location");
  }

  @Cuando("consulto el estudiante número {int}")
  public void consultoElEstudianteNumero(int id) {
    ultimaRespuesta = peticion().get("/api/estudiantes/" + id);
  }

  @Cuando("cambio su carrera a {string}")
  public void cambioSuCarreraA(String carrera) {
    var actual = peticion().get(ubicacionCreado).then().statusCode(200).extract().jsonPath();
    ultimaRespuesta =
        peticion()
            .body(
                unEstudiante()
                    .conNombre(actual.getString("nombre"))
                    .conCorreo(actual.getString("correo"))
                    .conCarrera(carrera)
                    .solicitud())
            .put(ubicacionCreado);
  }

  @Cuando("elimino a ese estudiante")
  public void eliminoAEseEstudiante() {
    ultimaRespuesta = peticion().delete(ubicacionCreado);
  }

  // ---- Entonces / Y ----

  @Entonces("el sistema confirma el registro")
  public void elSistemaConfirmaElRegistro() {
    // 201 Created y un encabezado Location que apunta al nuevo recurso.
    assertThat(ultimaRespuesta.statusCode()).isEqualTo(201);
    assertThat(ultimaRespuesta.header("Location")).matches("/api/estudiantes/\\d+");
  }

  @Y("el nuevo estudiante puede consultarse con el nombre {string}")
  public void elNuevoEstudiantePuedeConsultarse(String nombre) {
    peticion()
        .get(ubicacionCreado)
        .then()
        .statusCode(200)
        .body("nombre", org.hamcrest.Matchers.equalTo(nombre));
  }

  @Entonces("el sistema rechaza el registro")
  public void elSistemaRechazaElRegistro() {
    assertThat(ultimaRespuesta.statusCode()).isEqualTo(400);
  }

  @Y("indica un error en el campo {string}")
  public void indicaUnErrorEnElCampo(String campo) {
    ultimaRespuesta.then().body("errores.campo", hasItem(campo));
  }

  @Entonces("el sistema informa que ese estudiante no existe")
  public void elSistemaInformaQueNoExiste() {
    assertThat(ultimaRespuesta.statusCode()).isEqualTo(404);
    ultimaRespuesta.then().body("errores.campo", hasItem("id"));
  }

  @Entonces("el sistema acepta el cambio")
  public void elSistemaAceptaElCambio() {
    assertThat(ultimaRespuesta.statusCode()).isEqualTo(200);
  }

  @Y("el estudiante figura en la carrera {string}")
  public void elEstudianteFiguraEnLaCarrera(String carrera) {
    peticion()
        .get(ubicacionCreado)
        .then()
        .statusCode(200)
        .body("carrera", org.hamcrest.Matchers.equalTo(carrera));
  }

  @Entonces("el sistema confirma la eliminación")
  public void elSistemaConfirmaLaEliminacion() {
    assertThat(ultimaRespuesta.statusCode()).isEqualTo(204);
  }

  @Y("el estudiante {string} ya no aparece en la lista de estudiantes")
  public void yaNoApareceEnLaLista(String nombre) {
    peticion().get("/api/estudiantes").then().statusCode(200).body("nombre", not(hasItem(nombre)));
    peticion().get(ubicacionCreado).then().statusCode(404);
  }
}
