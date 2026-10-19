CREATE TABLE escuderia (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    pais VARCHAR(100),
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE usuario ADD COLUMN escuderia_id BIGINT REFERENCES escuderia(id);
CREATE INDEX idx_usuario_escuderia ON usuario (escuderia_id);

CREATE TABLE piloto (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    nacionalidad VARCHAR(100) NOT NULL,
    numero INTEGER NOT NULL,
    abreviatura VARCHAR(3) NOT NULL,
    rol VARCHAR(20) NOT NULL CHECK (rol IN ('TITULAR', 'SUPLENTE')),
    numero_licencia_fia VARCHAR(60) NOT NULL,
    foto_url VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id BIGINT NOT NULL REFERENCES categoria(id),
    temporada_id BIGINT NOT NULL REFERENCES temporada(id),
    escuderia_id BIGINT NOT NULL REFERENCES escuderia(id),
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP,
    CONSTRAINT uq_piloto_numero UNIQUE (categoria_id, temporada_id, numero),
    CONSTRAINT uq_piloto_abreviatura UNIQUE (categoria_id, temporada_id, abreviatura)
);

CREATE INDEX idx_piloto_activo ON piloto (activo);
CREATE INDEX idx_piloto_categoria ON piloto (categoria_id);
CREATE INDEX idx_piloto_temporada ON piloto (temporada_id);
CREATE INDEX idx_piloto_escuderia ON piloto (escuderia_id);

INSERT INTO escuderia (nombre, pais, activa, fecha_creacion) VALUES
    ('Alpine', 'Francia', TRUE, CURRENT_TIMESTAMP),
    ('Ferrari', 'Italia', TRUE, CURRENT_TIMESTAMP),
    ('McLaren', 'Reino Unido', TRUE, CURRENT_TIMESTAMP);

UPDATE usuario
SET escuderia_id = (
    SELECT e.id FROM escuderia e WHERE e.nombre = 'Alpine'
)
WHERE username = 'admin.escuderia';
