-- V10: roles internos de escuderia.
-- El rol global del usuario (rol_id) define el acceso a la plataforma; el
-- rol de escuderia define que puede hacer un miembro dentro de su escuderia
-- (por ejemplo, actualizar informacion). El administrativo de escuderia
-- asigna y revoca estos roles a los miembros de su escuderia.

ALTER TABLE usuario ADD COLUMN rol_escuderia VARCHAR(30);

ALTER TABLE usuario ADD CONSTRAINT chk_usuario_rol_escuderia
    CHECK (rol_escuderia IS NULL OR rol_escuderia IN ('ADMINISTRADOR', 'EDITOR', 'LECTOR'));

UPDATE usuario
SET rol_escuderia = 'ADMINISTRADOR'
WHERE rol_id = (SELECT id FROM rol WHERE nombre = 'ADMIN_ESCUDERIA');
