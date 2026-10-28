# TDD: regla "nombre único por carrera"

> **Regla de negocio:** no se permiten dos estudiantes con el mismo nombre en la misma carrera.
> El servicio la valida y la API responde **409 Conflict**; la base de datos la respalda con una
> restricción `UNIQUE`. El mismo nombre en **otra** carrera sí se permite.

La regla se implementó con TDD estricto en tres commits consecutivos. Cada paso se ejecutó con
`./gradlew spotlessCheck test integrationTest acceptanceTest --continue --no-daemon` (verde y refactor
con `--rerun` para evitar resultados en caché de Gradle) y la salida de abajo es la real.

| Paso | Commit | Resultado |
|---|---|---|
| Rojo | [`c5be60b`](https://github.com/vacax/crud-estudiante-qa/commit/c5be60b) `tdd(rojo): pruebas de la regla de nombre único por carrera` | **BUILD FAILED**: 10 pruebas nuevas fallan, las previas pasan |
| Verde | [`ca33610`](https://github.com/vacax/crud-estudiante-qa/commit/ca33610) `tdd(verde): implementar la regla de nombre único por carrera` | **BUILD SUCCESSFUL**: 66 + 27 + 10 |
| Refactor | [`6405006`](https://github.com/vacax/crud-estudiante-qa/commit/6405006) `tdd(refactor): la BD respalda la regla y se simplifica el código` | **BUILD SUCCESSFUL**: 66 + 28 + 10 |

Para reproducir cualquier paso:

```bash
git checkout c5be60b   # o ca33610 / 6405006
./gradlew test integrationTest acceptanceTest --continue
git checkout main
```

## 1. Rojo — `c5be60b`

Solo se escribieron pruebas en los tres niveles más el **andamiaje mínimo para compilar**
(`EstudianteDuplicadoException` vacía y `existeNombreEnCarrera` lanzando
`UnsupportedOperationException`). Así el rojo es un fallo de aserción, no de compilación: la prueba
falla porque el comportamiento no existe.

| Nivel | Pruebas nuevas | Fallan |
|---|---|---|
| Unit (`EstudianteServiceTest`) | 5 | 5 de 66 |
| Integración (`EstudianteRepositorioJdbcIT`, `RestriccionesBaseDatosIT`) | 5 | 4 de 27 (la quinta, "mismo nombre en otra carrera es válido", describe un caso permitido y pasa desde el principio) |
| Aceptación (escenario "Rechazar un estudiante duplicado en la misma carrera") | 1 | 1 de 10 |

Salida (extracto real):

```
Servicio de estudiantes > crear con nombre repetido en la misma carrera lanza EstudianteDuplicadoException FAILED
    Expecting code to raise a throwable.
Servicio de estudiantes > actualizar con nombre y carrera de otro estudiante lanza EstudianteDuplicadoException FAILED
    Expecting actual throwable to be an instance of:
      edu.pucmm.estudiante.dominio.EstudianteDuplicadoException
Servicio de estudiantes > crear con el mismo nombre en otra carrera se permite FAILED
    Wanted but not invoked:
    repositorio.existeNombreEnCarrera(
Servicio de estudiantes > crear sin duplicado consulta la regla y guarda FAILED
    Wanted but not invoked:
Servicio de estudiantes > actualizar conservando su propio nombre y carrera no lanza (se excluye a sí mismo) FAILED
    Wanted but not invoked:
66 tests completed, 5 failed
> Task :test FAILED

Repositorio JDBC de estudiantes (PostgreSQL real) > existeNombreEnCarrera ignora al estudiante excluido pero no a otros FAILED
    java.lang.UnsupportedOperationException: Pendiente: TDD paso verde
Repositorio JDBC de estudiantes (PostgreSQL real) > existeNombreEnCarrera es true si hay un estudiante con ese nombre y carrera FAILED
    java.lang.UnsupportedOperationException: Pendiente: TDD paso verde
Repositorio JDBC de estudiantes (PostgreSQL real) > existeNombreEnCarrera es false para otra carrera o para otro nombre FAILED
    java.lang.UnsupportedOperationException: Pendiente: TDD paso verde
Restricciones de la base de datos > UNIQUE rechaza dos estudiantes con el mismo nombre en la misma carrera FAILED
    Expecting code to raise a throwable.
27 tests completed, 4 failed
> Task :integrationTest FAILED

SuiteAceptacion > Gestión de estudiantes > Rechazar un estudiante duplicado en la misma carrera FAILED
    org.opentest4j.AssertionFailedError:
    expected: 409
     but was: 201
        at ✽.el sistema informa un conflicto(classpath:features/estudiantes.feature:23)
10 tests completed, 1 failed
> Task :acceptanceTest FAILED

BUILD FAILED in 33s
```

## 2. Verde — `ca33610`

Lo mínimo para pasar, **sin tocar las pruebas nuevas**:

- `V2__nombre_unico_por_carrera.sql`: `CONSTRAINT uq_estudiante_nombre_carrera UNIQUE (nombre, carrera)`.
- `EstudianteRepositorioJdbc.existeNombreEnCarrera`: `SELECT EXISTS (...)` excluyendo el propio id.
- `EstudianteService`: verifica duplicados al crear y al actualizar.
- `EstudianteControlador`: `EstudianteDuplicadoException` → `409` con `{"errores":[{"campo":"nombre",...}]}`.

Se ajustó **una prueba previa**, `MigracionesIT`, que fijaba "exactamente 1 migración"; con V2
la expectativa correcta es V1 y V2. No es "arreglar la prueba para que pase": la prueba describía un
hecho (el número de migraciones) que la nueva regla cambia legítimamente.

```
BUILD SUCCESSFUL in 25s
test 66/66 · integrationTest 27/27 · acceptanceTest 10/10
```

## 3. Refactor — `6405006`

Mejoras sin cambio de comportamiento observable:

1. **La BD respalda la regla ante concurrencia.** Dos peticiones simultáneas pueden pasar ambas la
   verificación del servicio; la segunda viola el `UNIQUE` y antes habría dado **500**. Ahora el
   repositorio traduce SQLState `23505` sobre `uq_estudiante_nombre_carrera` a
   `EstudianteDuplicadoException` (→ 409). Se añadió una prueba de integración de *protección*
   (guardar dos veces directo por el repositorio) que pasa al primer intento: documenta el
   refactor, no forma parte del ciclo rojo.
2. `EstudianteDuplicadoException(nombre, carrera)` arma su propio mensaje; el servicio queda en una línea.
3. `existeNombreEnCarrera` usa `id IS DISTINCT FROM ?` con un solo parámetro (antes repetía el
   parámetro con un cast).

```
BUILD SUCCESSFUL in 26s
test 66/66 · integrationTest 28/28 · acceptanceTest 10/10
```

## Lo que muestra el ciclo

- El rojo **falla por la razón correcta** (aserción), lo que prueba que las pruebas detectan la ausencia del comportamiento.
- La misma regla se especifica en tres niveles: el servicio (rápido, con Mockito), la BD real (restricción `UNIQUE`, SQLState 23505) y la API como la ve un usuario (409).
- El refactor se hace con la red de seguridad en verde; la carrera de condición es un ejemplo de algo que solo aparece al pensar en la BD real, no en el mock.
