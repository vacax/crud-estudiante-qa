# Entrega: niveles de prueba y QA en el pipeline

<!-- Copie este archivo a la raíz del repositorio como ENTREGA.md: cp docs/ENTREGA-PLANTILLA.md ENTREGA.md
     Guía: docs/GUIA-ESTUDIANTE.md. Reemplace todo lo que está entre < >. -->

| | |
|---|---|
| Nombre | <nombre completo> |
| Matrícula | <matrícula> |
| Fork | https://github.com/<su-usuario>/crud-estudiante-qa |
| Sistema operativo | <p. ej. Ubuntu 24.04 / Windows 11 + WSL2 / macOS 15> |
| CPU y RAM | <p. ej. 8 núcleos, 16 GB> |
| ¿Ejecutó todo en local? | <sí / no: qué partes hizo solo en CI y por qué> |

## Parte 1. Preparación

- [ ] Fork con todas las ramas (`git branch -a` muestra las cuatro `defecto/*`)
- [ ] GitHub Actions habilitado → `entrega/p1-actions.png`

## Parte 2. El sistema desde fuera

| # | Método y ruta | Código | Una línea del cuerpo o encabezado relevante |
|---|---|---|---|
| 1 | `POST /api/estudiantes` (válido) | | |
| 2 | `POST /api/estudiantes` (3 errores) | | |
| 3 | `POST /api/estudiantes` (duplicado) | | |
| 4 | `PUT /api/estudiantes/1` | | |
| 5a | `GET /api/estudiantes/9999` | | |
| 5b | `GET /api/estudiantes/abc` | | |
| 6a | `DELETE /api/estudiantes/1` | | |
| 6b | `DELETE /api/estudiantes/1` (otra vez) | | |
| 7 | `GET /health` sin BD | | |

**P2.1**

**P2.2**

**P2.3**

**P2.4**

## Parte 3. Línea base en `main`

| Nivel | Nº de pruebas | Tiempo `real` | ¿Docker? | ¿Navegador? |
|---|---|---|---|---|
| Unit | | | | |
| Integración | | | | |
| Aceptación | | | | |
| E2E | | | | |

**P3.1**

**P3.2**

| Nivel | Prueba (nombre exacto) | Qué comprueba que ningún nivel inferior puede |
|---|---|---|
| Unit | | |
| Integración | | |
| Aceptación | | |
| E2E | | |

**P3.3**

**P3.4**

## Parte 4. `defecto/unit`

**Mensaje del commit:** <git log -1 --format=%s origin/defecto/unit>

| Nivel | Predicción (pasa/falla) y por qué | Resultado real (pruebas / fallidas) |
|---|---|---|
| Unit | | |
| Integración | | |
| Aceptación | | |
| E2E | | |

**Diagnóstico** (una prueba fallida por nivel y su mensaje):

```
<pegue aquí>
```

**Cambio sembrado** (`git diff main defecto/unit -- src compose.yaml`):

```diff
<pegue aquí>
```

**P4.1**

**P4.2**

**P4.3**

**P4.4** (captura en `entrega/p4-captura.png`)

**P4.5**

## Parte 5. `defecto/integracion`

**Mensaje del commit:**

| Nivel | Predicción (pasa/falla) y por qué | Resultado real (pruebas / fallidas) |
|---|---|---|
| Unit | | |
| Integración | | |
| Aceptación | | |
| E2E | | |

**Diagnóstico:**

```
<pegue aquí>
```

**Cambio sembrado:**

```diff
<pegue aquí>
```

**P5.1**

**P5.2**

**P5.3**

**P5.4**

```gherkin
<paso propuesto>
```

## Parte 6. `defecto/aceptacion`

**Mensaje del commit:**

| Nivel | Predicción (pasa/falla) y por qué | Resultado real (pruebas / fallidas) |
|---|---|---|
| Unit | | |
| Integración | | |
| Aceptación | | |
| E2E | | |

**Diagnóstico:**

```
<pegue aquí>
```

**Cambio sembrado:**

```diff
<pegue aquí>
```

**P6.1**

**P6.2**

**P6.3**

**P6.4**

## Parte 7. `defecto/e2e`

**Mensaje del commit:**

| Nivel | Predicción (pasa/falla) y por qué | Resultado real (pruebas / fallidas) |
|---|---|---|
| Unit | | |
| Integración | | |
| Aceptación | | |
| E2E | | |

**Diagnóstico de las pruebas:**

```
<pegue aquí>
```

**Diagnóstico manual** (`docker compose ps -a` y `docker compose logs app`):

```
<pegue aquí>
```

**Cambio sembrado:**

```diff
<pegue aquí>
```

**P7.1**

**P7.2**

**P7.3**

**P7.4**

## Parte 8. CI en el fork

| Rama | Run (enlace) | Job en rojo | Jobs *skipped* | Duración total |
|---|---|---|---|---|
| `main` | | — | — | |
| `defecto/unit` | | | | |
| `defecto/integracion` | | | | |
| `defecto/aceptacion` | | | | |
| `defecto/e2e` | | | | |

Grafo de jobs de `defecto/integracion`: `entrega/p8-grafo.png`

**P8.1**

**P8.2** (muestre el cálculo)

**P8.3**

**P8.4**

| Rama | Artefacto recomendado |
|---|---|
| `defecto/unit` | |
| `defecto/integracion` | |
| `defecto/aceptacion` | |
| `defecto/e2e` | |

## Parte 9. TDD

| Commit | Paso | Unit (pruebas / fallidas) | Integración | Aceptación | Resultado del build |
|---|---|---|---|---|---|
| `c5be60b` | Rojo | | | | |
| `ca33610` | Verde | | | | |
| `6405006` | Refactor | | | | |

**P9.1**

**P9.2**

**P9.3**

## Parte 10. Cobertura frente a mutation testing

| Medición | Cobertura de líneas de `dominio` (JaCoCo, unit) | Mutantes generados | Muertos | *Mutation score* | ¿Pasó el gate? |
|---|---|---|---|---|---|
| Base | | | | | |
| Pruebas debilitadas | | | | | |

**P10.1**

**P10.2**

| Clase y línea | Descripción del mutante (PIT) | Caso de prueba que lo mataría |
|---|---|---|
| | | |

**P10.3**

**P10.4**

## Parte 11. Reto: `defecto/propio`

| | Commit | Run (enlace) | Resultado |
|---|---|---|---|
| Defecto | | | (debe ser verde) |
| Prueba | | | (debe ser rojo, job: ) |

**P11.1**

**P11.2**

**P11.3**

## Parte 12. Seguridad del pipeline

**P12.1**

**P12.2** (fuente: <enlace>)

**P12.3**

**P12.4**
