# language: es
Característica: Gestión de estudiantes
  Como encargado de la secretaría académica
  Quiero registrar, consultar, actualizar y eliminar estudiantes
  Para mantener al día el listado oficial de la institución

  Antecedentes:
    Dado que no hay estudiantes registrados

  Escenario: Registrar un estudiante válido
    Cuando registro al estudiante "Ana Pérez" de la carrera "ISC" con correo "ana.perez@ejemplo.edu"
    Entonces el sistema confirma el registro
    Y el nuevo estudiante puede consultarse con el nombre "Ana Pérez"

  Escenario: Rechazar un estudiante con una carrera inválida
    Cuando intento registrar un estudiante con nombre "Ana Pérez", carrera "XYZ" y correo "ana.perez@ejemplo.edu"
    Entonces el sistema rechaza el registro
    Y indica un error en el campo "carrera"

  Escenario: Consultar un estudiante inexistente
    Cuando consulto el estudiante número 9999
    Entonces el sistema informa que ese estudiante no existe

  Escenario: Actualizar la carrera de un estudiante
    Dado que existe el estudiante "Ana Pérez" de la carrera "ISC"
    Cuando cambio su carrera a "ITT"
    Entonces el sistema acepta el cambio
    Y el estudiante figura en la carrera "ITT"

  Escenario: Eliminar un estudiante y comprobar que ya no aparece
    Dado que existe el estudiante "Ana Pérez" de la carrera "ISC"
    Cuando elimino a ese estudiante
    Entonces el sistema confirma la eliminación
    Y el estudiante "Ana Pérez" ya no aparece en la lista de estudiantes

  # En la columna nombre, "[espacio]" representa un espacio (las celdas de la tabla se recortan).
  Esquema del escenario: Rechazar datos inválidos indicando el campo con error
    Cuando intento registrar un estudiante con nombre "<nombre>", carrera "<carrera>" y correo "<correo>"
    Entonces el sistema rechaza el registro
    Y indica un error en el campo "<campo>"

    Ejemplos:
      | descripcion                   | nombre                 | carrera | correo                | campo   |
      | nombre de un solo carácter    | A                      | ISC     | ana.perez@ejemplo.edu | nombre  |
      | nombre con espacios sobrantes | [espacio]Ana Pérez     | ISC     | ana.perez@ejemplo.edu | nombre  |
      | carrera fuera del catálogo    | Ana Pérez              | XYZ     | ana.perez@ejemplo.edu | carrera |
      | correo inválido               | Ana Pérez              | ISC     | ana.perez.ejemplo.edu | correo  |
