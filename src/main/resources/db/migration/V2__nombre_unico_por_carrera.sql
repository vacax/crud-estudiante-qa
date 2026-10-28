-- Regla de negocio (fase 5, TDD): un nombre no puede repetirse dentro de la misma carrera.
ALTER TABLE estudiante
    ADD CONSTRAINT uq_estudiante_nombre_carrera UNIQUE (nombre, carrera);
