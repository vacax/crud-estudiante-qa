package edu.pucmm.estudiante.e2e;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

/** Caja negra con REST Assured sobre el puerto publicado del stack. */
class CajaNegraE2ETest extends BaseE2E {

  @Test
  void saludYCicloDeVidaPorApi() {
    given().baseUri(urlBase).get("/health").then().statusCode(200);

    var respuesta =
        given()
            .baseUri(urlBase)
            .contentType("application/json")
            .body(
                "{\"nombre\":\"Grace Hopper\",\"carrera\":\"ITT\",\"correo\":\"grace@pucmm.edu.do\"}")
            .post("/api/estudiantes");
    // Solo se exige 2xx: el código exacto (201/Location) es asunto de aceptación.
    assertThat(respuesta.statusCode()).isBetween(200, 299);
    int id = respuesta.jsonPath().getInt("id");

    given()
        .baseUri(urlBase)
        .get("/api/estudiantes/{id}", id)
        .then()
        .statusCode(200)
        .body("nombre", equalTo("Grace Hopper"));
    given().baseUri(urlBase).delete("/api/estudiantes/{id}", id).then().statusCode(204);
    given().baseUri(urlBase).get("/api/estudiantes/{id}", id).then().statusCode(404);
  }
}
