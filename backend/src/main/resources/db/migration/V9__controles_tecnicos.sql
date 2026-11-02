-- V9: registro de controles tecnicos de los autos de las escuderias.
-- Un control asocia un auto (identificado por su codigo) a una escuderia,
-- una fecha, los elementos verificados, el resultado (APTO / NO_APTO) y
-- observaciones opcionales.

CREATE TABLE control_tecnico (
    id                   BIGSERIAL PRIMARY KEY,
    escuderia_id         BIGINT NOT NULL REFERENCES escuderia (id),
    piloto_id            BIGINT REFERENCES piloto (id) ON DELETE SET NULL,
    auto                 VARCHAR(150) NOT NULL,
    fecha                DATE NOT NULL,
    elementos_verificados VARCHAR(2000) NOT NULL,
    resultado            VARCHAR(20) NOT NULL CHECK (resultado IN ('APTO', 'NO_APTO')),
    observaciones        VARCHAR(2000),
    fecha_creacion       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_control_tecnico_escuderia ON control_tecnico (escuderia_id);
CREATE INDEX idx_control_tecnico_fecha ON control_tecnico (fecha);
CREATE INDEX idx_control_tecnico_piloto ON control_tecnico (piloto_id);

INSERT INTO control_tecnico (escuderia_id, piloto_id, auto, fecha, elementos_verificados, resultado, observaciones)
VALUES (
    (SELECT id FROM escuderia WHERE nombre = 'Ferrari'),
    (SELECT id FROM piloto WHERE numero = 99
        AND categoria_id = (SELECT id FROM categoria WHERE codigo = 'F1')
        AND temporada_id = (SELECT id FROM temporada WHERE anio = 2026)),
    'Ferrari SF-26 #99',
    DATE '2026-03-14',
    'Aleron delantero, aleron trasero, suelo, unidad de potencia',
    'APTO',
    'Verificacion previa al inicio de temporada sin observaciones.'
);

INSERT INTO control_tecnico (escuderia_id, auto, fecha, elementos_verificados, resultado, observaciones)
VALUES (
    (SELECT id FROM escuderia WHERE nombre = 'Alpine'),
    'Alpine A526 #10',
    DATE '2026-04-02',
    'Peso minimo, dimensiones del monoplaza, sistema de escape',
    'NO_APTO',
    'Se detecto un componente del sistema de escape fuera de la tolerancia reglamentaria.'
);
