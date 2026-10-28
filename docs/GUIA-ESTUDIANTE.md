# Guía práctica: niveles de prueba y QA en el pipeline

**Asignación de entrega.** En esta práctica usted hará un *fork* de este repositorio, ejecutará cada
nivel de prueba sobre el código correcto y sobre cuatro versiones con un defecto sembrado, observará
qué nivel lo atrapa (en su máquina y en GitHub Actions) y responderá preguntas de análisis con
**evidencia de sus propias ejecuciones**.

> Cada rama `defecto/*` es `main` más un único cambio, así que esta guía y la plantilla están en
> todas las ramas. Al viajar a los commits de TDD (Parte 9) no estarán: téngala abierta en GitHub.

## Contenido

- [Qué se entrega y cómo se evalúa](#qué-se-entrega-y-cómo-se-evalúa)
- [Parte 1. Preparar el fork](#parte-1-preparar-el-fork)
- [Parte 2. Conocer el sistema desde fuera](#parte-2-conocer-el-sistema-desde-fuera)
- [Parte 3. Línea base: los cuatro niveles en `main`](#parte-3-línea-base-los-cuatro-niveles-en-main)
- [Protocolo para cada defecto sembrado](#protocolo-para-cada-defecto-sembrado)
- [Parte 4. `defecto/unit`](#parte-4-defectounit)
- [Parte 5. `defecto/integracion`](#parte-5-defectointegracion)
- [Parte 6. `defecto/aceptacion`](#parte-6-defectoaceptacion)
- [Parte 7. `defecto/e2e`](#parte-7-defectoe2e)
- [Parte 8. Los mismos defectos en CI](#parte-8-los-mismos-defectos-en-ci)
- [Parte 9. TDD: rojo, verde, refactor](#parte-9-tdd-rojo-verde-refactor)
- [Parte 10. Cobertura frente a mutation testing](#parte-10-cobertura-frente-a-mutation-testing)
- [Parte 11. Reto: un defecto que nadie atrapa](#parte-11-reto-un-defecto-que-nadie-atrapa)
- [Parte 12. El pipeline como control de seguridad](#parte-12-el-pipeline-como-control-de-seguridad)
- [Parte 13. Entregar](#parte-13-entregar)
- [Problemas frecuentes](#problemas-frecuentes)

## Qué se entrega y cómo se evalúa

Se entrega **la URL de su fork**. En la rama `main` del fork debe estar:

1. `ENTREGA.md` (en la raíz), creado a partir de [`docs/ENTREGA-PLANTILLA.md`](ENTREGA-PLANTILLA.md),
   con todas las tablas llenas y las preguntas respondidas.
2. La carpeta `entrega/` con los logs y capturas que se piden en cada parte.
3. La rama `defecto/propio` del reto (Parte 11), con sus dos commits y sus runs de CI.

| Parte | Puntos |
|---|---|
| 1. Fork preparado y CI habilitado | 5 |
| 2. Sistema desde fuera | 5 |
| 3. Línea base | 10 |
| 4 a 7. Defectos sembrados (10 c/u) | 40 |
| 8. CI | 10 |
| 9. TDD | 5 |
| 10. Cobertura frente a mutación | 10 |
| 11. Reto | 10 |
| 12. Seguridad del pipeline | 5 |
| **Total** | **100** |

**Criterios de corrección de las preguntas:**

- Una respuesta **sin evidencia propia** (nombre exacto de la prueba, mensaje de error copiado de su
  log, número medido, enlace a su run) vale como máximo la mitad.
- Se valora más una **predicción equivocada bien analizada** que una predicción correcta sin
  explicación. No corrija sus predicciones después de ejecutar: se evalúa el contraste.
- Respuestas breves y precisas. Tres o cuatro oraciones bastan para casi todas.

> **Regla de juego:** el análisis debe salir de **sus** ejecuciones. Responder con análisis ajenos
> (de compañeros o de otras fuentes) se considera falta. Si una observación suya contradice lo que
> esperaba la guía, no la ajuste: explíquela, eso suma.

---

## Parte 1. Preparar el fork

### Requisitos

| Herramienta | Comprobación | Nota |
|---|---|---|
| Git | `git --version` | |
| JDK 25 | `java -version` debe decir `25` | Temurin recomendado. Gradle **no** se instala: viene el wrapper `./gradlew`. |
| Docker con `docker compose` | `docker version` y `docker compose version` | Docker Desktop (Windows/macOS) o Docker Engine (Linux). Su usuario debe poder correr `docker` sin `sudo`. |
| Cuenta de GitHub | | |

En **Windows**, trabaje dentro de **WSL2** (Ubuntu): los comandos de esta guía son de `bash`.

Necesitará unos 5 GB libres (imágenes Docker, Chromium de Playwright y caché de Gradle). La primera
ejecución descarga todo; las siguientes son mucho más rápidas.

> **Sin Docker en su máquina:** puede hacer las partes 3 a 7 corriendo todo en GitHub Actions
> (Parte 8) y descargando los artefactos de cada run. Indíquelo en su entrega. La Parte 2 y las
> preguntas que piden ejecutar `docker compose` las responderá leyendo los logs del job `e2e`.

### 1.1 Hacer el fork **con todas las ramas**

1. Abra <https://github.com/vacax/crud-estudiante-qa> y pulse **Fork**.
2. **Desmarque la casilla “Copy the `main` branch only”.** Si la deja marcada, su fork no tendrá las
   ramas `defecto/*`.
3. Pulse **Create fork**.

Si ya lo hizo con la casilla marcada, recupere las ramas después de clonar (paso 1.3):

```bash
git remote add upstream https://github.com/vacax/crud-estudiante-qa.git
git fetch upstream
for r in unit integracion aceptacion e2e; do
  git push origin "upstream/defecto/$r:refs/heads/defecto/$r"
done
# Quite el remoto: con las ramas en dos remotos, "git switch defecto/..." sería ambiguo.
git remote remove upstream
```

### 1.2 Habilitar GitHub Actions en el fork

GitHub desactiva los workflows en los forks. En su fork: pestaña **Actions** → botón
**“I understand my workflows, go ahead and enable them”**.

### 1.3 Clonar y comprobar

Reemplace `<su-usuario>` por su usuario de GitHub:

```bash
git clone https://github.com/<su-usuario>/crud-estudiante-qa.git
cd crud-estudiante-qa
git branch -a          # deben aparecer remotes/origin/defecto/{unit,integracion,aceptacion,e2e}
java -version
docker compose version
```

### 1.4 Crear su archivo de entrega

```bash
cp docs/ENTREGA-PLANTILLA.md ENTREGA.md
mkdir -p entrega
```

**No haga commit todavía.** `ENTREGA.md` y `entrega/` no están versionados, así que Git los
conserva intactos cuando cambie de rama. Se versionan al final (Parte 13). Haga copias de
respaldo si lo prefiere.

**Evidencia:** captura de la pestaña Actions de su fork habilitada → `entrega/p1-actions.png`.

---

## Parte 2. Conocer el sistema desde fuera

Antes de mirar pruebas, use la aplicación como lo haría un cliente de la API.

```bash
cp .env.ejemplo .env
docker compose up --build -d
docker compose ps                      # espere a que bd esté "healthy" y app "Up"
curl -s http://localhost:7070/health; echo
```

Ejecute una por una y observe **código de estado, encabezados y cuerpo** (`-i` los muestra):

```bash
# 1. Crear un estudiante válido
curl -si -X POST http://localhost:7070/api/estudiantes -H 'Content-Type: application/json' \
  -d '{"nombre":"Ana Pérez","carrera":"ISC","correo":"ana.perez@ejemplo.edu"}'; echo

# 2. Crear uno con TRES errores a la vez
curl -si -X POST http://localhost:7070/api/estudiantes -H 'Content-Type: application/json' \
  -d '{"nombre":"A","carrera":"isc","correo":"malo"}'; echo

# 3. Repetir nombre y carrera
curl -si -X POST http://localhost:7070/api/estudiantes -H 'Content-Type: application/json' \
  -d '{"nombre":"Ana Pérez","carrera":"ISC","correo":"otra@ejemplo.edu"}'; echo

# 4. Cambiar la carrera
curl -si -X PUT http://localhost:7070/api/estudiantes/1 -H 'Content-Type: application/json' \
  -d '{"nombre":"Ana Pérez","carrera":"ITT","correo":"ana.perez@ejemplo.edu"}'; echo

# 5. Consultar uno que no existe, y uno con id no numérico
curl -si http://localhost:7070/api/estudiantes/9999; echo
curl -si http://localhost:7070/api/estudiantes/abc; echo

# 6. Eliminar dos veces el mismo
curl -si -X DELETE http://localhost:7070/api/estudiantes/1; echo
curl -si -X DELETE http://localhost:7070/api/estudiantes/1; echo

# 7. Tirar la base de datos y preguntar por la salud
docker compose stop bd
curl -si http://localhost:7070/health; echo
```

Abra también <http://localhost:7070/> en el navegador, cree un estudiante e intente uno inválido.

Al terminar:

```bash
docker compose down -v
```

**Evidencia:** pegue en `ENTREGA.md` la tabla con método, ruta, código obtenido y una línea del
cuerpo de cada petición.

**Preguntas**

- **P2.1** En la respuesta de la petición 1, ¿qué valor trae el encabezado `Location`? ¿Para qué le
  sirve a un cliente de la API? ¿Qué tendría que hacer ese cliente si la API respondiera `200` sin
  ese encabezado?
- **P2.2** La petición 2 devolvió los tres errores juntos. ¿Por qué es mejor para el usuario que
  detenerse en el primero? Busque en `src/test` la prueba que garantiza ese comportamiento y cite su
  `@DisplayName`.
- **P2.3** Con la base de datos detenida, `/health` respondió `503`. Busque en el `Dockerfile` y en
  `src/e2eTest` quién consume esa ruta y qué decide con la respuesta.
- **P2.4** En la salida de `docker compose ps`, compare la columna `PORTS` de `app` y de `bd`.
  ¿Puede conectarse a PostgreSQL desde su máquina? ¿Por qué es una buena decisión de seguridad?

---

## Parte 3. Línea base: los cuatro niveles en `main`

Ejecute cada nivel **por separado** y mida el tiempo. `--rerun` obliga a ejecutar aunque Gradle
crea que nada cambió. La primera vez de cada nivel tardará más (descargas); si quiere un tiempo
representativo, ejecútelo dos veces y anote el segundo.

```bash
git switch main
time ./gradlew test --rerun            2>&1 | tee entrega/p3-unit.log
time ./gradlew integrationTest --rerun 2>&1 | tee entrega/p3-integracion.log
time ./gradlew acceptanceTest --rerun  2>&1 | tee entrega/p3-aceptacion.log
time ./gradlew e2eTest --rerun         2>&1 | tee entrega/p3-e2e.log
git show main:scripts/resumen-pruebas.sh | bash
```

El último comando resume cuántas pruebas corrió cada nivel. Abra también los reportes HTML:

| Nivel | Reporte |
|---|---|
| Unit | `build/reports/tests/test/index.html` |
| Integración | `build/reports/tests/integrationTest/index.html` |
| Aceptación | `build/reports/tests/acceptanceTest/index.html` y `build/reports/cucumber/cucumber.html` |
| E2E | `build/reports/tests/e2eTest/index.html` |

Lea, al menos por encima, una clase de prueba de cada nivel:
`src/test/.../ValidadorEstudianteTest.java`, `src/integrationTest/.../EstudianteRepositorioJdbcIT.java`,
`src/acceptanceTest/resources/features/estudiantes.feature` y
`src/e2eTest/.../FlujoNavegadorE2ETest.java`.

**Evidencia:** tabla en `ENTREGA.md` (nivel, nº de pruebas, tiempo `real`, ¿necesita Docker?, ¿usa
navegador?).

**Preguntas**

- **P3.1** Ordene los niveles por número de pruebas y por tiempo. ¿La forma que obtiene se parece a
  una pirámide? ¿Por qué tiene sentido que haya decenas de unitarias y solo un puñado de E2E?
- **P3.2** Elija **una** prueba de cada nivel (nombre exacto, tal como aparece en el reporte) y
  diga en una oración qué comprueba que **ningún nivel inferior** podría comprobar.
- **P3.3** El archivo `.feature` está escrito en español y sin tecnicismos. ¿Quién, además de un
  programador, podría leerlo y aprobarlo? ¿Qué ventaja tiene eso para definir “terminado”?
- **P3.4** `e2eTest` no forma parte de `./gradlew check`. Dé una razón a favor y un riesgo de esa
  decisión.

---

## Protocolo para cada defecto sembrado

Cada rama `defecto/*` contiene **un único cambio** de código o configuración respecto a `main`. Para
cada una (partes 4 a 7) siga exactamente estos pasos:

**A. Predecir (antes de ejecutar nada).** Lea solo el mensaje del commit:

```bash
git log -1 --format=%s origin/defecto/<rama>
```

Anote en `ENTREGA.md`, para cada nivel (unit, integración, aceptación, E2E), si cree que **pasará o
fallará**, y por qué en una línea. No la cambie después.

**B. Ejecutar los cuatro niveles** sin detenerse en el primer fallo (`--continue`). `clean` borra
los resultados de la rama anterior para que no se mezclen:

```bash
git switch defecto/<rama>
./gradlew clean test integrationTest acceptanceTest e2eTest --continue 2>&1 | tee entrega/<rama>.log
git show main:scripts/resumen-pruebas.sh | bash
```

Es normal que termine con `BUILD FAILED`: eso es lo que se quiere observar.

**C. Registrar.** Llene la tabla “predicción frente a realidad” con los números del resumen.

**D. Diagnosticar.** Para cada nivel que falló, copie de su log (o del reporte HTML) el **nombre de
una prueba fallida** y las **dos o tres líneas** del mensaje de error que mejor explican la falla.
Puede buscar en el log con:

```bash
grep -n -A6 "FAILED" entrega/<rama>.log | less
```

**E. Confirmar la causa.** Solo ahora mire el cambio sembrado:

```bash
git diff main defecto/<rama> -- src compose.yaml
```

**F. Responder** las preguntas de la parte.

**G. Volver a `main`** al terminar: `git switch main`.

---

## Parte 4. `defecto/unit`

Siga el protocolo con `<rama>` = `unit`.

Esta rama genera **artefactos de Playwright**: busque en `build/reports/e2e/capturas/` la captura
`.png` y en `build/reports/e2e/traces/` el `.zip`. Abra el trace arrastrándolo a
<https://trace.playwright.dev> (se procesa en su navegador; no se sube a ningún servidor). Copie la
captura a `entrega/p4-captura.png`.

**Preguntas**

- **P4.1** Compare su predicción con el resultado. ¿Qué nivel fue el que más le sorprendió y por qué?
- **P4.2** Copie el mensaje de error de cada nivel que falló. Si usted fuera quien debe corregir el
  defecto, ¿cuál de esos mensajes le lleva **más rápido** a la línea de código culpable? Justifique.
- **P4.3** En aceptación, la API no responde `400`. ¿Qué código responde? Siga el dato `"A"` desde
  el controlador hasta la base de datos: ¿qué componente impidió que se guardara un nombre
  inválido? (busque en `src/main/resources/db/migration`). ¿Vale la pena validar la misma regla en
  dos capas?
- **P4.4** Describa lo que ve el usuario en la captura y en el trace de Playwright. ¿Qué acción del
  trace le permitió saber en qué paso falló la prueba?
- **P4.5** ¿Por qué el nivel de integración pasa?

---

## Parte 5. `defecto/integracion`

Siga el protocolo con `<rama>` = `integracion`.

**Preguntas**

- **P5.1** Predicción frente a realidad, en dos o tres oraciones.
- **P5.2** El paquete `servicio` tiene 100 % de cobertura y sus pruebas unitarias pasan. ¿Por qué no
  detectan este defecto? Mire `EstudianteServiceTest`: ¿qué reemplaza Mockito y qué consecuencia
  tiene eso?
- **P5.3** Compare el mensaje de error de integración con el de E2E. ¿Cuál dice **qué campo** está
  mal y cuál solo dice que “algo no cambió”?
- **P5.4** El escenario de aceptación *“Registrar un estudiante válido”* **pasa** con este defecto.
  Lea sus pasos en `PasosEstudiante.java`: ¿qué no está comprobando? Escriba el paso Gherkin (en
  español) y la aserción que habría que añadir para que falle. *(Opcional, +2: impleméntelo en una
  rama aparte y muestre que falla en `defecto/integracion` y pasa en `main`.)*

---

## Parte 6. `defecto/aceptacion`

Siga el protocolo con `<rama>` = `aceptacion`.

**Preguntas**

- **P6.1** Predicción frente a realidad.
- **P6.2** La prueba E2E **pasa**. Lea `CajaNegraE2ETest.java` y `src/main/resources/public/app.js`
  y explique por qué. ¿Es un hueco de la prueba E2E o una decisión de diseño correcta? Argumente
  su postura.
- **P6.3** Fallan cuatro escenarios, pero el defecto es uno. ¿Cuál escenario falla por la causa raíz
  y cuáles fallan “en cascada” por un paso `Dado`? ¿Cómo lo distinguió en el reporte?
- **P6.4** Describa un cliente real (aplicación móvil, otro servicio, un script) que **sí** se
  rompería en producción con este defecto aunque la página web funcione.

---

## Parte 7. `defecto/e2e`

Siga el protocolo con `<rama>` = `e2e`. **Ojo:** el nivel E2E tardará varios minutos en fallar.

Después, diagnostique el mismo problema **a mano**, sin pruebas:

```bash
git switch defecto/e2e
docker compose up --build -d
docker compose ps -a          # ¿en qué estado quedó cada servicio?
docker compose logs app       # ¿qué dice la aplicación al morir?
docker compose down -v
git switch main
```

**Preguntas**

- **P7.1** Predicción frente a realidad.
- **P7.2** ¿Cuánto tardó en fallar el nivel E2E y por qué tanto? (busque `withStartupTimeout` en
  `src/e2eTest`). ¿Cuánto tardó usted en encontrar la causa con `docker compose logs`? ¿Qué
  mejora propondría al pipeline para que ese diagnóstico aparezca solo cuando E2E falla?
- **P7.3** No cambió **ninguna línea de Java**. ¿Por qué los otros tres niveles no pueden detectar
  este defecto? Proponga **dos ejemplos más** de defectos que solo una prueba E2E encontraría (que
  no sean de este repositorio).
- **P7.4** En esta rama no se genera captura ni trace de Playwright. ¿Por qué? Entonces, ¿dónde está
  la evidencia del fallo?

---

## Parte 8. Los mismos defectos en CI

Ahora ejecute el pipeline de su fork sobre `main` y sobre las cuatro ramas. Como las ramas ya
existen en el fork, no hace falta hacer push: lance el workflow a mano.

**Opción web:** pestaña **Actions** → workflow **CI** → **Run workflow** → elija la rama →
**Run workflow**. Repita para `main` y las cuatro `defecto/*`.

**Opción con `gh`** (GitHub CLI, si lo tiene):

```bash
for r in main defecto/unit defecto/integracion defecto/aceptacion defecto/e2e; do
  gh workflow run ci.yml --ref "$r" -R <su-usuario>/crud-estudiante-qa
done
gh run list -R <su-usuario>/crud-estudiante-qa --limit 5
```

Los runs corren en paralelo; el de `main` tarda unos 10 minutos. Mientras, abra el run de
`defecto/unit` y observe el grafo de jobs.

De **un** run fallido, descargue el artefacto `reportes-*` del job en rojo (al final de la página del
run, sección *Artifacts*), descomprímalo y abra su `index.html`.

**Evidencia:**

- Tabla en `ENTREGA.md`: rama, enlace al run, job en rojo, jobs *skipped*, duración total.
- Captura del grafo de jobs del run de `defecto/integracion` → `entrega/p8-grafo.png`.

**Preguntas**

- **P8.1** Compare la tabla de CI con lo que observó en local. En `defecto/unit`, localmente fallaron
  varios niveles; en CI, ¿cuántos jobs fallaron y cuántos quedaron *skipped*? Explique qué es *fail
  fast* y qué información se pierde a cambio.
- **P8.2** Con las duraciones de los jobs del run de `main`, calcule cuántos minutos de runner se
  ahorran en `defecto/unit` gracias a *fail fast*, frente a correr todos los jobs. ¿Por qué los jobs
  están ordenados así y no al revés?
- **P8.3** En `defecto/e2e`, el job `build-imagen` termina en verde y el `e2e` en rojo. ¿Qué le dice
  eso sobre la diferencia entre “la imagen se construye” y “el sistema funciona”?
- **P8.4** Un compañero ve su run en rojo y no tiene el código clonado. ¿Qué artefacto le
  recomendaría descargar para diagnosticar, en cada una de las cuatro ramas?

---

## Parte 9. TDD: rojo, verde, refactor

La regla *“no puede haber dos estudiantes con el mismo nombre en la misma carrera”* se programó con
TDD en tres commits consecutivos. Ejecútelos en orden:

```bash
for c in c5be60b ca33610 6405006; do
  git checkout -q "$c"
  git log -1 --format='== %h %s'
  ./gradlew clean test integrationTest acceptanceTest --continue 2>&1 | tee "entrega/p9-$c.log" | tail -3
  git show main:scripts/resumen-pruebas.sh | bash
done
git switch main
```

Mire qué cambió en cada paso con `git show --stat <hash>` y `git show <hash> -- src/main`.

**Evidencia:** tabla en `ENTREGA.md` con, por commit, pruebas y fallas de cada nivel.

**Preguntas**

- **P9.1** En el commit rojo, ¿las pruebas fallan por **error de compilación** o por **aserción**?
  Muestre una línea de su log que lo pruebe. ¿Por qué importa esa diferencia?
- **P9.2** Compare `git show --stat` del commit rojo y del verde: ¿en qué carpetas hay cambios en
  cada uno? ¿Qué regla de TDD se ve reflejada?
- **P9.3** El refactor traduce una violación de `UNIQUE` de PostgreSQL a un `409`. Describa paso a
  paso el escenario de **dos peticiones simultáneas** que hace necesario ese cambio. ¿Por qué una
  prueba unitaria con Mockito nunca lo habría revelado?

---

## Parte 10. Cobertura frente a mutation testing

**Paso 1. Medición base** (en `main`):

```bash
git switch main
./gradlew clean test jacocoTestReport pitest 2>&1 | tee entrega/p10-base.log | grep '>>'
```

Anote: la cobertura de líneas del paquete `dominio` en `build/reports/jacoco/test/html/index.html`, y el
*mutation score* que imprime PIT (`Generated N mutations Killed M`). Abra
`build/reports/pitest/index.html` y entre a `ValidadorEstudiante.java`.

**Paso 2. Debilitar las pruebas sin borrar ninguna.** Quite los **valores límite** de dos pruebas
parametrizadas (deja 3 y 99 como válidos y 500 como inválido):

```bash
sed -i.bak \
  -e 's/@ValueSource(ints = {2, 3, 99, 100})/@ValueSource(ints = {3, 99})/' \
  -e 's/@ValueSource(ints = {1, 101, 500})/@ValueSource(ints = {500})/' \
  src/test/java/edu/pucmm/estudiante/dominio/ValidadorEstudianteTest.java
git diff --stat
./gradlew test jacocoTestReport pitest 2>&1 | tee entrega/p10-debil.log | grep '>>'
```

Anote los mismos números. En el reporte de PIT busque los mutantes `SURVIVED` de
`ValidadorEstudiante` (en rojo).

**Paso 3. Restaurar:**

```bash
git checkout -- src/test && rm -f src/test/java/edu/pucmm/estudiante/dominio/ValidadorEstudianteTest.java.bak
git status --short     # solo deben aparecer ENTREGA.md y entrega/
```

**Preguntas**

- **P10.1** ¿Cambió la cobertura de líneas del dominio? ¿Cambió el *mutation score*? Explique por qué
  uno se movió y el otro no.
- **P10.2** ¿Qué mutantes sobrevivieron? Indique línea y descripción tal como la da PIT, y explique
  con el concepto de **análisis de valores límite** qué casos de prueba los matarían.
- **P10.3** El build tiene `mutationThreshold = 85` (en `build.gradle`). ¿Pasó o falló el gate con
  las pruebas debilitadas? ¿Qué le enseña eso sobre fijar umbrales? Proponga una política mejor.
- **P10.4** Incluso en la medición base sobrevive un mutante en `Carrera.desdeTexto`. Léalo y
  explique por qué **ninguna** prueba podría matarlo. ¿Qué nombre recibe ese tipo de mutante?

---

## Parte 11. Reto: un defecto que nadie atrapa

Hasta ahora todos los defectos fueron atrapados por algún nivel. Su tarea es encontrar un hueco.

1. Cree su rama desde `main`:

   ```bash
   git switch main
   git switch -c defecto/propio
   ```

2. **Commit 1, el defecto.** Modifique **solo** `src/main` para introducir un error **realista**
   (algo que un programador podría cometer de verdad) que, sin embargo, deje los cuatro niveles en
   verde. Compruébelo:

   ```bash
   ./gradlew clean spotlessApply test integrationTest acceptanceTest e2eTest --continue 2>&1 | tee entrega/p11-defecto.log | tail -3
   git add src/main && git commit -m "defecto(propio): <descripción breve>"
   git push -u origin defecto/propio
   ```

   El push a `defecto/**` dispara el CI solo. El run debe quedar **en verde**: su defecto escapó.

3. **Commit 2, la prueba.** Sin tocar `src/main`, escriba la prueba que lo atrapa, **en el nivel más
   bajo posible**. Compruebe que falla, haga commit y push:

   ```bash
   git add src/test src/integrationTest src/acceptanceTest src/e2eTest
   git commit -m "test(propio): <qué comprueba la prueba>"
   git push
   ```

   El nuevo run debe quedar **en rojo**, en el job del nivel que eligió.

4. Vuelva a `main` (`git switch main`). **No** fusione esta rama.

Pistas para buscar huecos: ¿qué campos de una respuesta **no** compara ninguna prueba? ¿Qué rutas
o códigos de error de la tabla de la API no aparecen en ningún `.feature`? ¿Qué devuelve el cuerpo
de un `PUT`?

**Evidencia:** enlaces a los dos runs (verde y rojo) en `ENTREGA.md`.

**Preguntas**

- **P11.1** Describa su defecto y explique por qué cada uno de los cuatro niveles lo dejó pasar.
- **P11.2** ¿En qué nivel escribió la prueba y por qué ese y no uno superior? Si tuvo que subir de
  nivel, explique qué le impidió hacerlo más abajo.
- **P11.3** Después de esto, ¿qué significa realmente “todas las pruebas pasan”? Responda en una o
  dos oraciones.

---

## Parte 12. El pipeline como control de seguridad

Lea `.github/workflows/ci.yml`, el `Dockerfile`, `compose.yaml` y `.gitignore`.

**Preguntas**

- **P12.1** El workflow declara `permissions: {}` arriba y cada job pide solo `contents: read` (uno
  pide además `checks: write`). Si una Action de terceros fuera comprometida, ¿qué podría hacer con
  un token de escritura que no puede hacer con este?
- **P12.2** Las Actions se fijan por **SHA completo** (`@3d3c42e…`) y no por etiqueta (`@v7`).
  Investigue un incidente real de cadena de suministro en GitHub Actions (cite la fuente) y explique
  cómo esta práctica lo habría mitigado.
- **P12.3** Identifique en el `Dockerfile` dos decisiones que reducen el impacto si la aplicación es
  vulnerada, y en `compose.yaml` / `.gitignore` una que protege credenciales.
- **P12.4** ¿Considera los gates de calidad de este pipeline (formato, pruebas, cobertura, mutación)
  un control de seguridad? Argumente, con un ejemplo de un defecto **de esta aplicación** que, sin
  pruebas, se convertiría en un problema de seguridad o de integridad de datos.

---

## Parte 13. Entregar

```bash
git switch main
git status --short                       # deben aparecer ENTREGA.md y entrega/
git add ENTREGA.md entrega/
git commit -m "entrega: guía de niveles de prueba"
git push
```

El push a `main` dispara el CI de su fork: debe quedar en verde (no tocó código).

**Lista de comprobación antes de entregar:**

- [ ] `ENTREGA.md` en la raíz de `main`, con todas las tablas llenas y todas las preguntas respondidas.
- [ ] Las predicciones de las partes 4 a 7 están escritas y no fueron corregidas.
- [ ] `entrega/` contiene los logs y capturas pedidos.
- [ ] Enlaces a los runs de CI de las cinco ramas (Parte 8) y a los dos de `defecto/propio` (Parte 11).
- [ ] La rama `defecto/propio` está publicada en el fork y **no** fue fusionada.
- [ ] El último run de `main` está en verde.

Envíe por la plataforma del curso **la URL de su fork**.

---

## Problemas frecuentes

| Síntoma | Causa probable y solución |
|---|---|
| `git branch -a` no muestra las ramas `defecto/*` | Hizo el fork con *“Copy the main branch only”*. Vea el bloque del paso 1.1. |
| La pestaña Actions no muestra runs | Los workflows están desactivados en el fork: paso 1.2. |
| `Could not find a valid Docker environment` (Testcontainers) | Docker no está corriendo o su usuario no tiene permiso. Pruebe `docker ps`. En Linux: `sudo usermod -aG docker $USER` y vuelva a iniciar sesión. |
| `permission denied` al ejecutar `./gradlew` | `chmod +x gradlew` |
| `Toolchain ... 25 ... not found` / `release version 25 not supported` | No tiene JDK 25, o `JAVA_HOME` apunta a otro. Revise `java -version` y `echo $JAVA_HOME`. |
| El puerto 7070 está ocupado (Parte 2) | Cambie `APP_PUERTO` en `.env` (por ejemplo `7071`) y use ese puerto en `curl`. Las pruebas E2E usan un puerto aleatorio y no se ven afectadas. |
| E2E falla en Linux con errores de librerías de Chromium | Instale las dependencias del sistema: `./gradlew instalarNavegadores -PplaywrightArgs="install --with-deps chromium"` (pide `sudo`). |
| `spotlessCheck` falla en el reto | Ejecute `./gradlew spotlessApply` antes de hacer commit. |
| Al cambiar de rama Git dice que perdería cambios | Tiene cambios sin guardar en archivos versionados (por ejemplo, el experimento de la Parte 10). Restaure con `git checkout -- src`. `ENTREGA.md` y `entrega/` no cuentan. |
| El resumen dice “sin resultados” para un nivel | Ese nivel no llegó a ejecutarse (por ejemplo, falló la compilación). Revise el log de esa parte. |
