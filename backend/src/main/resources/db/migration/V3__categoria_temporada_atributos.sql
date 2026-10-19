CREATE TABLE categoria (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE temporada (
    id BIGSERIAL PRIMARY KEY,
    anio INTEGER NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_inicio DATE,
    fecha_fin DATE
);

CREATE TABLE categoria_atributo (
    id BIGSERIAL PRIMARY KEY,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    clave VARCHAR(80) NOT NULL,
    valor VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uq_categoria_atributo UNIQUE (categoria_id, clave)
);

CREATE INDEX idx_categoria_atributo_categoria ON categoria_atributo (categoria_id);

INSERT INTO categoria (codigo, nombre, activa) VALUES
    ('F1', 'Fórmula 1', TRUE),
    ('F2', 'Fórmula 2', TRUE),
    ('F3', 'Fórmula 3', TRUE),
    ('F1_ACADEMY', 'F1 Academy', TRUE);

INSERT INTO temporada (anio, nombre, activa, fecha_inicio, fecha_fin) VALUES
    (2026, 'Temporada 2026', TRUE, DATE '2026-01-01', DATE '2026-12-31');

INSERT INTO categoria_atributo (categoria_id, clave, valor, descripcion)
SELECT c.id, 'tipo_competencia', 'Circuito', 'Tipo de competencia principal verificado en el calendario vigente.'
FROM categoria c WHERE c.codigo = 'F1';

INSERT INTO categoria_atributo (categoria_id, clave, valor, descripcion)
SELECT c.id, 'tipo_competencia', 'Circuito', 'Tipo de competencia principal verificado en el calendario vigente.'
FROM categoria c WHERE c.codigo = 'F2';

INSERT INTO categoria_atributo (categoria_id, clave, valor, descripcion)
SELECT c.id, 'tipo_competencia', 'Circuito', 'Tipo de competencia principal verificado en el calendario vigente.'
FROM categoria c WHERE c.codigo = 'F3';

INSERT INTO categoria_atributo (categoria_id, clave, valor, descripcion)
SELECT c.id, 'tipo_competencia', 'Copa de desarrollo', 'Se valida con el equipo si corresponde al formato de la categoría.'
FROM categoria c WHERE c.codigo = 'F1_ACADEMY';
