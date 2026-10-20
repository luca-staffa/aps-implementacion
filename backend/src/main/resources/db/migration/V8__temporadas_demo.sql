-- V8: datos de demostracion para las temporadas 2024 y 2025.
-- Objetivo: que el calendario y la vista consolidada tengan actividad real en mas de un anio y
-- que los filtros por temporada (incluido "Todas las temporadas") sean verificables en la UI.

INSERT INTO temporada (anio, nombre, activa, fecha_inicio, fecha_fin)
SELECT 2025, 'Temporada 2025', TRUE, DATE '2025-01-01', DATE '2025-12-31'
WHERE NOT EXISTS (SELECT 1 FROM temporada WHERE anio = 2025);

INSERT INTO temporada (anio, nombre, activa, fecha_inicio, fecha_fin)
SELECT 2024, 'Temporada 2024', TRUE, DATE '2024-01-01', DATE '2024-12-31'
WHERE NOT EXISTS (SELECT 1 FROM temporada WHERE anio = 2024);

-- Calendario 2025
INSERT INTO evento_calendario (categoria, nombre, circuito, pais, fecha_inicio, fecha_fin, tipo) VALUES
    ('F1', 'Gran Premio de Australia 2025', 'Albert Park Circuit', 'Australia', DATE '2025-03-14', DATE '2025-03-16', 'CARRERA'),
    ('F1', 'Gran Premio de Emilia-Romagna 2025', 'Autodromo Enzo e Dino Ferrari', 'Italia', DATE '2025-05-16', DATE '2025-05-18', 'CARRERA'),
    ('F1', 'Gran Premio de Gran Bretana 2025', 'Silverstone Circuit', 'Reino Unido', DATE '2025-07-04', DATE '2025-07-06', 'CARRERA'),
    ('F1', 'Gran Premio de Brasil 2025', 'Autodromo Jose Carlos Pace', 'Brasil', DATE '2025-11-07', DATE '2025-11-09', 'CARRERA'),

    ('F2', 'Ronda de Bahrein 2025', 'Bahrain International Circuit', 'Bahrein', DATE '2025-04-11', DATE '2025-04-13', 'CARRERA'),
    ('F2', 'Ronda de Monaco 2025', 'Circuit de Monaco', 'Monaco', DATE '2025-05-23', DATE '2025-05-25', 'CARRERA'),
    ('F2', 'Ronda de Italia 2025', 'Autodromo Nazionale Monza', 'Italia', DATE '2025-09-05', DATE '2025-09-07', 'CARRERA'),

    ('F3', 'Ronda de Australia 2025', 'Albert Park Circuit', 'Australia', DATE '2025-03-14', DATE '2025-03-16', 'CARRERA'),
    ('F3', 'Ronda de Espana 2025', 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2025-05-30', DATE '2025-06-01', 'CARRERA'),
    ('F3', 'Ronda de Hungria 2025', 'Hungaroring', 'Hungria', DATE '2025-08-01', DATE '2025-08-03', 'CARRERA'),

    ('F1_ACADEMY', 'Ronda de China 2025', 'Shanghai International Circuit', 'China', DATE '2025-03-21', DATE '2025-03-23', 'CARRERA'),
    ('F1_ACADEMY', 'Ronda de Paises Bajos 2025', 'Circuit Zandvoort', 'Paises Bajos', DATE '2025-08-29', DATE '2025-08-31', 'CARRERA');

-- Calendario 2024
INSERT INTO evento_calendario (categoria, nombre, circuito, pais, fecha_inicio, fecha_fin, tipo) VALUES
    ('F1', 'Gran Premio de Bahrein 2024', 'Bahrain International Circuit', 'Bahrein', DATE '2024-02-29', DATE '2024-03-02', 'CARRERA'),
    ('F1', 'Gran Premio de Monaco 2024', 'Circuit de Monaco', 'Monaco', DATE '2024-05-24', DATE '2024-05-26', 'CARRERA'),
    ('F1', 'Gran Premio de Italia 2024', 'Autodromo Nazionale Monza', 'Italia', DATE '2024-08-30', DATE '2024-09-01', 'CARRERA'),
    ('F1', 'Gran Premio de Abu Dabi 2024', 'Yas Marina Circuit', 'Emiratos Arabes Unidos', DATE '2024-12-06', DATE '2024-12-08', 'CARRERA'),

    ('F2', 'Ronda de Bahrein 2024', 'Bahrain International Circuit', 'Bahrein', DATE '2024-02-29', DATE '2024-03-02', 'CARRERA'),
    ('F2', 'Ronda de Espana 2024', 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2024-06-21', DATE '2024-06-23', 'CARRERA'),
    ('F2', 'Ronda de Qatar 2024', 'Lusail International Circuit', 'Qatar', DATE '2024-11-29', DATE '2024-12-01', 'CARRERA'),

    ('F3', 'Ronda de Bahrein 2024', 'Bahrain International Circuit', 'Bahrein', DATE '2024-02-29', DATE '2024-03-02', 'CARRERA'),
    ('F3', 'Ronda de Gran Bretana 2024', 'Silverstone Circuit', 'Reino Unido', DATE '2024-07-05', DATE '2024-07-07', 'CARRERA'),
    ('F3', 'Ronda de Belgica 2024', 'Circuit de Spa-Francorchamps', 'Belgica', DATE '2024-07-26', DATE '2024-07-28', 'CARRERA'),

    ('F1_ACADEMY', 'Ronda de Jeddah 2024', 'Jeddah Corniche Circuit', 'Arabia Saudita', DATE '2024-03-07', DATE '2024-03-09', 'CARRERA'),
    ('F1_ACADEMY', 'Ronda de Miami 2024', 'Miami International Autodrome', 'Estados Unidos', DATE '2024-05-03', DATE '2024-05-05', 'CARRERA');

-- Pruebas de neumaticos de las temporadas anteriores.
INSERT INTO prueba_neumatico (anio, circuito, pais, fecha_inicio, fecha_fin, categorias, aviso_publicado) VALUES
    (2025, 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2025-02-11', DATE '2025-02-13', 'F1,F2', TRUE),
    (2025, 'Circuito de Jerez', 'Espana', DATE '2025-03-04', DATE '2025-03-05', 'F3,F1_ACADEMY', FALSE),
    (2024, 'Circuit de Barcelona-Catalunya', 'Espana', DATE '2024-02-13', DATE '2024-02-15', 'F1,F2', TRUE),
    (2024, 'Circuito de Jerez', 'Espana', DATE '2024-03-05', DATE '2024-03-06', 'F3,F1_ACADEMY', FALSE);

-- Pilotos de demostracion por temporada (la combinacion categoria+temporada+numero es unica).
INSERT INTO piloto (nombre, apellido, fecha_nacimiento, nacionalidad, numero, abreviatura,
                    rol, numero_licencia_fia, foto_url, activo, categoria_id, temporada_id,
                    escuderia_id)
VALUES
    ('Alexander', 'Voss', DATE '1998-04-12', 'Alemania', 11, 'VOS',
     'TITULAR', 'FIA-2025-011', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'Alpine')),
    ('Mateo', 'Rivas', DATE '1997-09-03', 'Espana', 22, 'RIV',
     'TITULAR', 'FIA-2025-022', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'Ferrari')),
    ('Luca', 'Ferrante', DATE '2001-01-25', 'Italia', 33, 'FER',
     'TITULAR', 'FIA-2025-033', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F2'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'McLaren')),
    ('Noah', 'Kim', DATE '2002-06-18', 'Corea del Sur', 44, 'KIM',
     'SUPLENTE', 'FIA-2025-044', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F2'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'Alpine')),
    ('Diego', 'Salas', DATE '2003-11-30', 'Mexico', 55, 'SAL',
     'TITULAR', 'FIA-2025-055', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F3'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'Ferrari')),
    ('Hugo', 'Bernard', DATE '2004-02-14', 'Francia', 66, 'BER',
     'SUPLENTE', 'FIA-2025-066', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F3'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'McLaren')),
    ('Emma', 'Lorenz', DATE '2004-07-08', 'Alemania', 77, 'LOR',
     'TITULAR', 'FIA-2025-077', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1_ACADEMY'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'Alpine')),
    ('Sofia', 'Marin', DATE '2005-03-21', 'Espana', 88, 'MAR',
     'TITULAR', 'FIA-2025-088', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1_ACADEMY'),
     (SELECT id FROM temporada WHERE anio = 2025),
     (SELECT id FROM escuderia WHERE nombre = 'McLaren')),

    ('Bruno', 'Costa', DATE '1996-05-19', 'Brasil', 11, 'COS',
     'TITULAR', 'FIA-2024-011', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'Ferrari')),
    ('Liam', 'Hart', DATE '1999-08-27', 'Reino Unido', 22, 'HAR',
     'TITULAR', 'FIA-2024-022', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'McLaren')),
    ('Pablo', 'Nieto', DATE '2000-12-02', 'Argentina', 33, 'NIE',
     'TITULAR', 'FIA-2024-033', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F2'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'Alpine')),
    ('Kenji', 'Mori', DATE '2001-10-11', 'Japon', 44, 'MOR',
     'SUPLENTE', 'FIA-2024-044', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F2'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'Ferrari')),
    ('Ivan', 'Petrov', DATE '2002-04-05', 'Bulgaria', 55, 'PET',
     'TITULAR', 'FIA-2024-055', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F3'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'McLaren')),
    ('Marco', 'Rossi', DATE '2003-06-23', 'Italia', 66, 'ROS',
     'SUPLENTE', 'FIA-2024-066', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F3'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'Alpine')),
    ('Clara', 'Vidal', DATE '2004-01-17', 'Espana', 77, 'VID',
     'TITULAR', 'FIA-2024-077', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1_ACADEMY'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'Ferrari')),
    ('Nina', 'Weber', DATE '2005-09-09', 'Alemania', 88, 'WEB',
     'TITULAR', 'FIA-2024-088', NULL, TRUE,
     (SELECT id FROM categoria WHERE codigo = 'F1_ACADEMY'),
     (SELECT id FROM temporada WHERE anio = 2024),
     (SELECT id FROM escuderia WHERE nombre = 'McLaren'));

-- Sanciones de demostracion, vinculadas a una carrera de la misma categoria y temporada.
INSERT INTO sancion (escuderia_registrada_id, categoria, evento_id, motivo, fecha, tipo, descripcion, activo)
VALUES
    ((SELECT id FROM escuderia WHERE nombre = 'Ferrari'), 'F1',
     (SELECT MIN(id) FROM evento_calendario
      WHERE categoria = 'F1' AND fecha_inicio = DATE '2025-03-14'),
     'Adelantamiento bajo bandera amarilla', DATE '2025-03-16', 'DEPORTIVA',
     'Sancion aplicada durante el Gran Premio de Australia 2025.', TRUE),
    ((SELECT id FROM escuderia WHERE nombre = 'McLaren'), 'F2',
     (SELECT MIN(id) FROM evento_calendario
      WHERE categoria = 'F2' AND fecha_inicio = DATE '2025-04-11'),
     'Componente no conforme en verificacion tecnica', DATE '2025-04-13', 'TECNICA',
     'Sancion aplicada en la ronda de Bahrein 2025.', TRUE),
    ((SELECT id FROM escuderia WHERE nombre = 'McLaren'), 'F1',
     (SELECT MIN(id) FROM evento_calendario
      WHERE categoria = 'F1' AND fecha_inicio = DATE '2024-05-24'),
     'Contacto evitable en la salida', DATE '2024-05-26', 'DEPORTIVA',
     'Sancion aplicada durante el Gran Premio de Monaco 2024.', TRUE),
    ((SELECT id FROM escuderia WHERE nombre = 'Alpine'), 'F3',
     (SELECT MIN(id) FROM evento_calendario
      WHERE categoria = 'F3' AND fecha_inicio = DATE '2024-07-05'),
     'Uso indebido del limite de pista', DATE '2024-07-07', 'DEPORTIVA',
     'Sancion aplicada en la ronda de Gran Bretana 2024.', TRUE);
