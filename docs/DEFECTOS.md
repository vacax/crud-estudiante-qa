# Defectos sembrados

Cuatro ramas `defecto/*`, cada una con **un solo cambio** respecto a `main`, sirven para mostrar en
clase para qué sirve cada nivel de prueba. Cada rama corre el mismo workflow de CI que `main`.

| Rama | Defecto sembrado | Run de CI |
|---|---|---|
| [`defecto/unit`](https://github.com/vacax/crud-estudiante-qa/compare/main...defecto/unit) | La validación acepta nombres de un solo carácter | [run](https://github.com/vacax/crud-estudiante-qa/actions/runs/40634834230) |
| [`defecto/integracion`](https://github.com/vacax/crud-estudiante-qa/compare/main...defecto/integracion) | El mapeo JDBC lee el correo de la columna equivocada | [run](https://github.com/vacax/crud-estudiante-qa/actions/runs/40634834328) |
| [`defecto/aceptacion`](https://github.com/vacax/crud-estudiante-qa/compare/main...defecto/aceptacion) | `POST /api/estudiantes` no responde con el contrato HTTP esperado | [run](https://github.com/vacax/crud-estudiante-qa/actions/runs/40634834520) |
| [`defecto/e2e`](https://github.com/vacax/crud-estudiante-qa/compare/main...defecto/e2e) | Variable de entorno de la BD mal nombrada en `compose.yaml` | [run](https://github.com/vacax/crud-estudiante-qa/actions/runs/40634834322) |

Run de referencia en `main` (todo en verde):
<https://github.com/vacax/crud-estudiante-qa/actions/runs/40633394350>

Qué detecta cada nivel en cada rama, y por qué, es el objeto de la asignación
[`GUIA-ESTUDIANTE.md`](GUIA-ESTUDIANTE.md) (partes 4 a 8).

## Cómo reproducir

```bash
git switch defecto/unit        # o integracion / aceptacion / e2e
./gradlew clean test integrationTest acceptanceTest e2eTest --continue
git switch main
```
