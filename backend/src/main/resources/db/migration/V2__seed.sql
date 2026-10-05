-- Semilla inicial del backend FIA.
-- Las contrasenas corresponden a "Password123!" hasheadas con BCrypt.
-- Adicionalmente, DataInitializer garantiza y re-hashea estas credenciales al arrancar.

INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMIN_FIA', 'Administrador general de la FIA'),
    ('ADMIN_ESCUDERIA', 'Administrador de escuderia'),
    ('PUBLICO', 'Usuario publico');

INSERT INTO permiso (nombre, descripcion, rol_id) VALUES
    ('SANCION_CREAR', 'Crear sanciones', (SELECT id FROM rol WHERE nombre = 'ADMIN_FIA')),
    ('SANCION_EDITAR', 'Editar sanciones', (SELECT id FROM rol WHERE nombre = 'ADMIN_FIA')),
    ('SANCION_ELIMINAR', 'Eliminar sanciones', (SELECT id FROM rol WHERE nombre = 'ADMIN_FIA')),
    ('CALENDARIO_VER', 'Consultar el calendario', (SELECT id FROM rol WHERE nombre = 'PUBLICO')),
    ('PRUEBAS_VER', 'Consultar pruebas de neumaticos', (SELECT id FROM rol WHERE nombre = 'PUBLICO'));

INSERT INTO usuario (nombre, apellido, email, username, password, rol_id, activo, ultima_actividad, fecha_creacion) VALUES
    ('Admin', 'FIA', 'admin@fia.com', 'admin.fia',
     '$2a$10$upc7cz/FjuBGkTa1E7zwbuEsPWsP6Uw3bfYHJTI1FJHtq6lRuoUQu',
     (SELECT id FROM rol WHERE nombre = 'ADMIN_FIA'), TRUE, NOW(), NOW()),
    ('Admin', 'Escuderia', 'escuderia@fia.com', 'admin.escuderia',
     '$2a$10$gR11lqPOQGLVz45xxOXRVOk/wdjrF3rVGDHWWlIX2G1IX9KqfK4Xu',
     (SELECT id FROM rol WHERE nombre = 'ADMIN_ESCUDERIA'), TRUE, NOW(), NOW()),
    ('Usuario', 'Publico', 'publico@fia.com', 'publico',
     '$2a$10$CutNvq0U/MYokv6ccMor4e8aDtG.O0Sy56qZ.L0HxTzFGGeKrGf0e',
     (SELECT id FROM rol WHERE nombre = 'PUBLICO'), TRUE, NOW(), NOW()),
    ('Piloto', 'Demo', 'piloto@fia.com', 'piloto.demo',
     '$2a$10$CutNvq0U/MYokv6ccMor4e8aDtG.O0Sy56qZ.L0HxTzFGGeKrGf0e',
     (SELECT id FROM rol WHERE nombre = 'PUBLICO'), TRUE, NOW(), NOW());

INSERT INTO evento_calendario (categoria, nombre, circuito, pais, fecha_inicio, fecha_fin, tipo) VALUES
    ('F1', 'Gran Premio de Italia', 'Autodromo Nazionale Monza', 'Italia', DATE '2026-09-04', DATE '2026-09-06', 'CARRERA'),
    ('F2', 'Ronda de Barein', 'Circuito Internacional de Barein', 'Barein', DATE '2026-03-20', DATE '2026-03-22', 'CARRERA'),
    ('F3', 'Ronda de Espana', 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2026-05-29', DATE '2026-05-31', 'CARRERA'),
    ('F1_ACADEMY', 'Ronda de Francia', 'Circuit Paul Ricard', 'Francia', DATE '2026-06-19', DATE '2026-06-21', 'CARRERA');

INSERT INTO sancion (piloto_id, escuderia_id, categoria, motivo, fecha, tipo, descripcion) VALUES
    (NULL, (SELECT id FROM usuario WHERE username = 'admin.escuderia'), 'F1',
     'Incumplimiento del reglamento tecnico', DATE '2026-04-15', 'TECNICA',
     'Se detecto un componente no conforme durante la verificacion posterior a la carrera.'),
    ((SELECT id FROM usuario WHERE username = 'piloto.demo'), NULL, 'F2',
     'Maniobra peligrosa en pista', DATE '2026-05-02', 'DEPORTIVA',
     'Advertencia por contacto evitable durante la sesion clasificatoria.');

INSERT INTO prueba_neumatico (anio, circuito, pais, fecha_inicio, fecha_fin, categorias, aviso_publicado) VALUES
    (2026, 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2026-02-10', DATE '2026-02-12', 'F1,F2', TRUE),
    (2026, 'Circuito de Jerez', 'Espana', DATE '2026-03-03', DATE '2026-03-04', 'F3,F1_ACADEMY', FALSE);

INSERT INTO resultado_prueba (prueba_id, escuderia, piloto, mejor_tiempo, vueltas, posicion) VALUES
    ((SELECT id FROM prueba_neumatico WHERE anio = 2026 AND circuito = 'Circuit de Barcelona-Catalunya'),
     'Escuderia Alfa', 'Piloto Uno', '1:18.452', 62, 1),
    ((SELECT id FROM prueba_neumatico WHERE anio = 2026 AND circuito = 'Circuit de Barcelona-Catalunya'),
     'Escuderia Beta', 'Piloto Dos', '1:18.910', 58, 2),
    ((SELECT id FROM prueba_neumatico WHERE anio = 2026 AND circuito = 'Circuito de Jerez'),
     'Escuderia Gamma', 'Piloto Tres', '1:24.301', 70, 1),
    ((SELECT id FROM prueba_neumatico WHERE anio = 2026 AND circuito = 'Circuito de Jerez'),
     'Escuderia Delta', 'Piloto Cuatro', '1:24.755', 65, 2);