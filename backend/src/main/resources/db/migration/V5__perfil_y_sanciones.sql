ALTER TABLE usuario ADD COLUMN telefono VARCHAR(30);

ALTER TABLE sancion ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE sancion ADD COLUMN evento_id BIGINT REFERENCES evento_calendario (id);

CREATE INDEX idx_sancion_activo ON sancion (activo);
CREATE INDEX idx_sancion_evento ON sancion (evento_id);
