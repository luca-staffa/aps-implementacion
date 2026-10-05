-- Esquema base del backend FIA (comision Violeta)

CREATE TABLE rol (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

CREATE TABLE usuario (
    id               BIGSERIAL PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    email            VARCHAR(150) NOT NULL UNIQUE,
    username         VARCHAR(80)  NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,
    rol_id           BIGINT REFERENCES rol (id),
    activo           BOOLEAN NOT NULL DEFAULT TRUE,
    ultima_actividad TIMESTAMP,
    fecha_creacion   TIMESTAMP
);
CREATE INDEX idx_usuario_rol ON usuario (rol_id);

CREATE TABLE permiso (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    rol_id      BIGINT REFERENCES rol (id)
);
CREATE INDEX idx_permiso_rol ON permiso (rol_id);

CREATE TABLE sancion (
    id           BIGSERIAL PRIMARY KEY,
    piloto_id    BIGINT REFERENCES usuario (id),
    escuderia_id BIGINT REFERENCES usuario (id),
    categoria    VARCHAR(30),
    motivo       VARCHAR(255) NOT NULL,
    fecha        DATE NOT NULL,
    tipo         VARCHAR(80),
    descripcion  VARCHAR(2000),
    CONSTRAINT chk_sancion_parte CHECK (piloto_id IS NOT NULL OR escuderia_id IS NOT NULL)
);
CREATE INDEX idx_sancion_fecha ON sancion (fecha);
CREATE INDEX idx_sancion_categoria ON sancion (categoria);
CREATE INDEX idx_sancion_piloto ON sancion (piloto_id);
CREATE INDEX idx_sancion_escuderia ON sancion (escuderia_id);

CREATE TABLE sancion_visto (
    id          BIGSERIAL PRIMARY KEY,
    sancion_id  BIGINT NOT NULL REFERENCES sancion (id) ON DELETE CASCADE,
    usuario_id  BIGINT NOT NULL REFERENCES usuario (id),
    fecha_visto TIMESTAMP
);
CREATE INDEX idx_visto_sancion ON sancion_visto (sancion_id);
CREATE INDEX idx_visto_usuario ON sancion_visto (usuario_id);

CREATE TABLE evento_calendario (
    id           BIGSERIAL PRIMARY KEY,
    categoria    VARCHAR(30) NOT NULL,
    nombre       VARCHAR(200) NOT NULL,
    circuito     VARCHAR(200),
    pais         VARCHAR(100),
    fecha_inicio DATE NOT NULL,
    fecha_fin    DATE,
    tipo         VARCHAR(80)
);
CREATE INDEX idx_evento_categoria ON evento_calendario (categoria);
CREATE INDEX idx_evento_fechas ON evento_calendario (fecha_inicio, fecha_fin);
CREATE INDEX idx_evento_circuito ON evento_calendario (circuito);

CREATE TABLE prueba_neumatico (
    id              BIGSERIAL PRIMARY KEY,
    anio            INTEGER NOT NULL,
    circuito        VARCHAR(200),
    pais            VARCHAR(100),
    fecha_inicio    DATE,
    fecha_fin       DATE,
    categorias      VARCHAR(200),
    aviso_publicado BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_prueba_anio ON prueba_neumatico (anio);

CREATE TABLE resultado_prueba (
    id           BIGSERIAL PRIMARY KEY,
    prueba_id    BIGINT NOT NULL REFERENCES prueba_neumatico (id) ON DELETE CASCADE,
    escuderia    VARCHAR(150),
    piloto       VARCHAR(150),
    mejor_tiempo VARCHAR(50),
    vueltas      INTEGER,
    posicion     INTEGER
);
CREATE INDEX idx_resultado_prueba ON resultado_prueba (prueba_id);