-- V7: las sanciones pasan a referenciar las entidades reales piloto y escuderia.
-- Las columnas legacy piloto_id/escuderia_id (que apuntaban a usuario) se dejan
-- intactas por compatibilidad; las nuevas columnas se agregan sin recrear indices
-- ni claves foraneas existentes.

DELETE FROM sancion_visto;
DELETE FROM sancion;

ALTER TABLE sancion DROP CONSTRAINT chk_sancion_parte;

ALTER TABLE sancion ADD COLUMN piloto_registrado_id BIGINT REFERENCES piloto (id);
ALTER TABLE sancion ADD COLUMN escuderia_registrada_id BIGINT REFERENCES escuderia (id);
ALTER TABLE sancion ADD CONSTRAINT chk_sancion_parte_registrada
    CHECK (piloto_registrado_id IS NOT NULL OR escuderia_registrada_id IS NOT NULL);

CREATE INDEX idx_sancion_piloto_registrado ON sancion (piloto_registrado_id);
CREATE INDEX idx_sancion_escuderia_registrada ON sancion (escuderia_registrada_id);

-- Piloto de demo para que el modulo de sanciones tenga datos asociados.
INSERT INTO piloto (nombre, apellido, fecha_nacimiento, nacionalidad, numero, abreviatura,
                    rol, numero_licencia_fia, foto_url, activo, categoria_id, temporada_id,
                    escuderia_id, fecha_creacion)
SELECT 'Piloto', 'Demo', DATE '2000-01-01', 'Argentina', 99, 'PDE',
       'TITULAR', 'FIA-DEMO-99', NULL, TRUE,
       (SELECT id FROM categoria WHERE codigo = 'F1'),
       (SELECT id FROM temporada WHERE anio = 2026),
       (SELECT id FROM escuderia WHERE nombre = 'Ferrari'),
       CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM piloto p
    WHERE p.numero = 99
      AND p.categoria_id = (SELECT id FROM categoria WHERE codigo = 'F1')
      AND p.temporada_id = (SELECT id FROM temporada WHERE anio = 2026)
);

INSERT INTO sancion (escuderia_registrada_id, categoria, evento_id, motivo, fecha, tipo, descripcion, activo)
VALUES ((SELECT id FROM escuderia WHERE nombre = 'Ferrari'),
        'F1',
        (SELECT MIN(id) FROM evento_calendario WHERE categoria = 'F1'),
        'Incumplimiento del reglamento tecnico',
        DATE '2026-04-15',
        'TECNICA',
        'Se detecto un componente no conforme durante la verificacion posterior a la carrera.',
        TRUE);

INSERT INTO sancion (piloto_registrado_id, categoria, evento_id, motivo, fecha, tipo, descripcion, activo)
VALUES ((SELECT id FROM piloto WHERE numero = 99
             AND categoria_id = (SELECT id FROM categoria WHERE codigo = 'F1')
             AND temporada_id = (SELECT id FROM temporada WHERE anio = 2026)),
        'F2',
        (SELECT MIN(id) FROM evento_calendario WHERE categoria = 'F2'),
        'Maniobra peligrosa en pista',
        DATE '2026-05-02',
        'DEPORTIVA',
        'Advertencia por contacto evitable durante la sesion clasificatoria.',
        TRUE);
