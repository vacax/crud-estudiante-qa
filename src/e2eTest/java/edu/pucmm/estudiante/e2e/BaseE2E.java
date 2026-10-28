package edu.pucmm.estudiante.e2e;

import java.io.File;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Levanta el stack completo (app + PostgreSQL) con el MISMO {@code compose.yaml} del proyecto.
 *
 * <p>El contenedor es estático: se arranca una vez por clase (costoso) y se comparte entre sus
 * pruebas. Con Testcontainers 2.x {@code ComposeContainer} usa siempre el CLI local {@code docker
 * compose}.
 */
abstract class BaseE2E {

  static final String SERVICIO_APP = "app";
  static final int PUERTO_APP = 7070;

  static ComposeContainer stack;
  static String urlBase;

  @BeforeAll
  static void levantarStack() {
    File compose = new File(System.getProperty("compose.archivo", "compose.yaml"));
    stack =
        new ComposeContainer(compose)
            // Puerto del host aleatorio: no choca con nada que ya escuche en 7070.
            .withEnv("APP_PUERTO", "0")
            .withExposedService(
                SERVICIO_APP,
                PUERTO_APP,
                Wait.forHttp("/health")
                    .forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)))
            .withStartupTimeout(Duration.ofMinutes(3));
    stack.start();
    urlBase =
        "http://"
            + stack.getServiceHost(SERVICIO_APP, PUERTO_APP)
            + ":"
            + stack.getServicePort(SERVICIO_APP, PUERTO_APP);
  }

  @AfterAll
  static void detenerStack() {
    if (stack != null) {
      stack.stop();
    }
  }
}
