package edu.pucmm.estudiante.e2e;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Extensión JUnit: gestiona Playwright (Chromium headless) y, por prueba, un contexto con tracing.
 * Si la prueba falla guarda el trace y una captura en el directorio de artefactos; si pasa, el
 * trace se descarta.
 */
final class RegistroPlaywright
    implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

  private final Path directorio =
      Path.of(System.getProperty("e2e.artefactos", "build/reports/e2e"));

  private Playwright playwright;
  private Browser navegador;
  private BrowserContext contexto;
  private Page pagina;

  Page pagina() {
    return pagina;
  }

  @Override
  public void beforeAll(ExtensionContext ctx) {
    playwright = Playwright.create();
    navegador = playwright.chromium().launch();
  }

  @Override
  public void beforeEach(ExtensionContext ctx) {
    contexto = navegador.newContext();
    contexto
        .tracing()
        .start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true));
    pagina = contexto.newPage();
  }

  @Override
  public void afterEach(ExtensionContext ctx) throws Exception {
    String nombre = ctx.getRequiredTestMethod().getName();
    boolean fallo = ctx.getExecutionException().isPresent();
    if (fallo) {
      Path capturas = Files.createDirectories(directorio.resolve("capturas"));
      Path traces = Files.createDirectories(directorio.resolve("traces"));
      try {
        pagina.screenshot(
            new Page.ScreenshotOptions()
                .setPath(capturas.resolve(nombre + ".png"))
                .setFullPage(true));
      } catch (RuntimeException e) {
        System.err.println("No se pudo capturar la pantalla: " + e.getMessage());
      }
      contexto.tracing().stop(new Tracing.StopOptions().setPath(traces.resolve(nombre + ".zip")));
    } else {
      contexto.tracing().stop();
    }
    contexto.close();
  }

  @Override
  public void afterAll(ExtensionContext ctx) {
    if (navegador != null) {
      navegador.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }
}
