-- Tabla de estudiantes. La restricción UNIQUE(nombre, carrera) llega en la fase 5 (TDD).
CREATE TABLE estudiante (
    id      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre  VARCHAR(100) NOT NULL,
    carrera VARCHAR(3)   NOT NULL,
    correo  VARCHAR(254) NOT NULL,
    CONSTRAINT ck_estudiante_nombre_longitud CHECK (char_length(nombre) >= 2),
    CONSTRAINT ck_estudiante_nombre_sin_bordes CHECK (nombre = btrim(nombre)),
    CONSTRAINT ck_estudiante_carrera CHECK (carrera IN ('ISC', 'ITT', 'IME', 'IIS', 'ICI'))
);
