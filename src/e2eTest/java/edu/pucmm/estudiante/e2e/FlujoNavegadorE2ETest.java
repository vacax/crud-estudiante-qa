package edu.pucmm.estudiante.e2e;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Flujo completo desde el navegador, contra la imagen real + PostgreSQL. Solo usa data-testid. */
class FlujoNavegadorE2ETest extends BaseE2E {

  @RegisterExtension static final RegistroPlaywright registro = new RegistroPlaywright();

  @Test
  void crearEditarYEliminarEstudiante() {
    Page page = registro.pagina();
    page.navigate(urlBase + "/");
    assertThat(page.getByTestId("tabla-estudiantes")).isVisible();

    // Crear
    page.getByTestId("campo-nombre").fill("Ada Lovelace");
    page.getByTestId("select-carrera").selectOption("ISC");
    page.getByTestId("campo-correo").fill("ada@pucmm.edu.do");
    page.getByTestId("boton-guardar").click();

    Locator fila =
        page.locator("[data-testid^='fila-estudiante-']")
            .filter(new Locator.FilterOptions().setHasText("Ada Lovelace"));
    assertThat(fila).hasCount(1);
    assertThat(fila).containsText("ISC");
    String testidFila = fila.getAttribute("data-testid");
    String id = testidFila.substring("fila-estudiante-".length());

    // Editar: cambiar la carrera
    page.getByTestId("boton-editar-" + id).click();
    assertThat(page.getByTestId("campo-nombre")).hasValue("Ada Lovelace");
    page.getByTestId("select-carrera").selectOption("IIS");
    page.getByTestId("boton-guardar").click();
    assertThat(page.getByTestId("fila-estudiante-" + id)).containsText("IIS");
    assertThat(page.getByTestId("fila-estudiante-" + id)).not().containsText("ISC");

    // Eliminar
    page.getByTestId("boton-eliminar-" + id).click();
    assertThat(page.getByTestId("fila-estudiante-" + id)).hasCount(0);
  }

  @Test
  void muestraErroresDeValidacionPorCampo() {
    Page page = registro.pagina();
    page.navigate(urlBase + "/");
    page.getByTestId("campo-nombre").fill("A");
    page.getByTestId("campo-correo").fill("no-es-un-correo");
    page.getByTestId("boton-guardar").click();
    assertThat(page.getByTestId("error-nombre")).not().isEmpty();
    assertThat(page.getByTestId("error-carrera")).not().isEmpty();
    assertThat(page.getByTestId("error-correo")).not().isEmpty();
    assertThat(page.getByTestId("tabla-estudiantes"))
        .not()
        .containsText(Pattern.compile("no-es-un-correo"));
  }
}
